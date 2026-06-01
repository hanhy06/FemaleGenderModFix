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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public record BreastWear3dRenderCommand(
    BreastWear3dModel model,
    int light,
    int overlay,
    int color
) implements SubmitNodeCollector.CustomGeometryRenderer {
    public BreastWear3dRenderCommand(BreastWear3dModel model, LivingEntityRenderState state, int overlay, int color) {
        this(model, state.lightCoords, overlay, color);
    }

    @Override
    public void render(@NotNull PoseStack.Pose matricesEntry, @NotNull VertexConsumer vertexConsumer) {
        model.render(matricesEntry, vertexConsumer, light, overlay, color);
    }
}
