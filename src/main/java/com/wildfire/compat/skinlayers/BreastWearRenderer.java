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

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.render.BreastRenderCommand;
import com.wildfire.render.BreastSide;
import com.wildfire.render.WildfireModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface BreastWearRenderer {
    @SuppressWarnings("Convert2Lambda")
    BreastWearRenderer DEFAULT = new BreastWearRenderer() {
        @Override
        public void render(PoseStack matrixStack, SubmitNodeCollector queue, RenderType renderLayer, HumanoidRenderState state,
                           int overlay, int color, BreastSide side, WildfireModelRenderer.OverlayModelBox model,
                           UVLayout uvLayout, @Nullable Identifier skinTexture) {
            queue.submitCustomGeometry(matrixStack, renderLayer, new BreastRenderCommand(model, state, overlay, color));
        }
    };

    void render(PoseStack matrixStack, SubmitNodeCollector queue, RenderType renderLayer, HumanoidRenderState state,
                int overlay, int color, BreastSide side, WildfireModelRenderer.OverlayModelBox model,
                UVLayout uvLayout, @Nullable Identifier skinTexture);
}
