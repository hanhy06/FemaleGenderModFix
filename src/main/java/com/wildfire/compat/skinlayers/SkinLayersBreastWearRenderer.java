/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.compat.skinlayers;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.render.BreastSide;
import com.wildfire.render.WildfireModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class SkinLayersBreastWearRenderer implements BreastWearRenderer {
    private static final int MAX_MESHES = 128;

    private final BreastWearRenderer fallback;
    private final Map<MeshKey, BreastWear3dModel> meshes = new HashMap<>();

    public SkinLayersBreastWearRenderer(BreastWearRenderer fallback) {
        this.fallback = fallback;
    }

    @Override
    public void render(PoseStack matrixStack, SubmitNodeCollector queue, RenderType renderLayer, HumanoidRenderState state,
                       int overlay, int color, BreastSide side, WildfireModelRenderer.OverlayModelBox model,
                       UVLayout uvLayout, @Nullable Identifier skinTexture) {
        if (skinTexture == null) {
            fallback.render(matrixStack, queue, renderLayer, state, overlay, color, side, model, uvLayout, null);
            return;
        }

        BreastWear3dModel mesh = getMesh(skinTexture, side, uvLayout);
        if (mesh == null || mesh.isEmpty()) {
            fallback.render(matrixStack, queue, renderLayer, state, overlay, color, side, model, uvLayout, skinTexture);
            return;
        }

        queue.submitCustomGeometry(matrixStack, renderLayer, new BreastWear3dRenderCommand(mesh, state, overlay, color));
    }

    private @Nullable BreastWear3dModel getMesh(Identifier skinTexture, BreastSide side, UVLayout uvLayout) {
        MeshKey key = MeshKey.of(skinTexture, side, uvLayout);
        BreastWear3dModel cached = meshes.get(key);
        if (cached != null) return cached;

        SkinImage skin = getSkin(skinTexture);
        if (skin == null) return null;

        BreastWear3dModel mesh;
        try {
            if (!isSupportedSkin(skin.image())) return null;
            mesh = BreastWear3dModel.create(skin.image(), side, uvLayout);
        } finally {
            if (skin.owned()) {
                skin.image().close();
            }
        }

        if (meshes.size() >= MAX_MESHES) {
            meshes.clear();
        }
        meshes.put(key, mesh);
        return mesh;
    }

    private @Nullable SkinImage getSkin(Identifier skinTexture) {
        Minecraft client = Minecraft.getInstance();
        AbstractTexture texture = client.getTextureManager().getTexture(skinTexture);
        if (texture instanceof DynamicTexture dynamicTexture) {
            return new SkinImage(dynamicTexture.getPixels(), false);
        }

        Optional<Resource> resource = client.getResourceManager().getResource(skinTexture);
        if (resource.isEmpty()) return null;

        try (InputStream stream = resource.get().open()) {
            return new SkinImage(NativeImage.read(stream), true);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static boolean isSupportedSkin(@Nullable NativeImage skin) {
        return skin != null && !skin.isClosed() && skin.getWidth() == 64 && skin.getHeight() == 64;
    }

    private record MeshKey(Identifier skinTexture, BreastSide side, List<QuadKey> uvLayout) {
        private static MeshKey of(Identifier skinTexture, BreastSide side, UVLayout uvLayout) {
            List<QuadKey> quads = new ArrayList<>();
            for (UVDirection direction : UVDirection.values()) {
                quads.add(QuadKey.of(uvLayout.get(direction)));
            }
            return new MeshKey(skinTexture, side, List.copyOf(quads));
        }
    }

    private record QuadKey(int x1, int y1, int x2, int y2) {
        private static QuadKey of(@Nullable UVQuad quad) {
            if (quad == null) return new QuadKey(0, 0, 0, 0);
            return new QuadKey(quad.x1(), quad.y1(), quad.x2(), quad.y2());
        }
    }

    private record SkinImage(NativeImage image, boolean owned) {
    }
}
