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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.render.BreastSide;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BreastWear3dModel {
    private static final float PIXEL_DEPTH = 0.28f;
    private static final float TEXTURE_SIZE = 64f;

    private final List<Quad> quads;

    private BreastWear3dModel(List<Quad> quads) {
        this.quads = List.copyOf(quads);
    }

    public static BreastWear3dModel create(NativeImage skin, BreastSide side, UVLayout uvLayout) {
        List<Quad> quads = new ArrayList<>();
        for (UVDirection direction : UVDirection.values()) {
            UVQuad uv = uvLayout.get(direction);
            if (isEmpty(uv)) continue;

            Face face = Face.of(side, direction);
            for (int u = uv.x1(); u < uv.x2(); u++) {
                for (int v = uv.y1(); v < uv.y2(); v++) {
                    if (!hasPixel(skin, u, v)) continue;

                    addPixel(quads, skin, face, uv, u, v);
                }
            }
        }
        return new BreastWear3dModel(quads);
    }

    public boolean isEmpty() {
        return quads.isEmpty();
    }

    public void render(PoseStack.Pose entry, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        for (Quad quad : quads) {
            Vector3f normal = new Vector3f(quad.normal).mul(matrix3f);
            for (Vertex vertex : quad.vertices) {
                Vector4f position = new Vector4f(vertex.x / 16f, vertex.y / 16f, vertex.z / 16f, 1.0F).mul(matrix4f);
                vertexConsumer.addVertex(position.x(), position.y(), position.z(), color, vertex.u(), vertex.v(),
                    overlay, light, normal.x(), normal.y(), normal.z());
            }
        }
    }

    private static void addPixel(List<Quad> quads, NativeImage skin, Face face, UVQuad uv, int u, int v) {
        float s0 = (float) (u - uv.x1()) / (uv.x2() - uv.x1());
        float s1 = (float) (u + 1 - uv.x1()) / (uv.x2() - uv.x1());
        float t0 = (float) (v - uv.y1()) / (uv.y2() - uv.y1());
        float t1 = (float) (v + 1 - uv.y1()) / (uv.y2() - uv.y1());

        Vector3f topRight = face.point(s1, t0);
        Vector3f topLeft = face.point(s0, t0);
        Vector3f bottomLeft = face.point(s0, t1);
        Vector3f bottomRight = face.point(s1, t1);

        Vector3f offset = new Vector3f(face.normal).mul(PIXEL_DEPTH);
        Vector3f outerTopRight = new Vector3f(topRight).add(offset);
        Vector3f outerTopLeft = new Vector3f(topLeft).add(offset);
        Vector3f outerBottomLeft = new Vector3f(bottomLeft).add(offset);
        Vector3f outerBottomRight = new Vector3f(bottomRight).add(offset);

        addQuad(quads, face.normal, u, v, outerTopRight, outerTopLeft, outerBottomLeft, outerBottomRight);

        if (!hasPixel(skin, u - 1, v, uv)) {
            addQuad(quads, face.leftNormal(), u, v, outerTopLeft, topLeft, bottomLeft, outerBottomLeft);
        }
        if (!hasPixel(skin, u + 1, v, uv)) {
            addQuad(quads, face.rightNormal(), u, v, topRight, outerTopRight, outerBottomRight, bottomRight);
        }
        if (!hasPixel(skin, u, v - 1, uv)) {
            addQuad(quads, face.upNormal(), u, v, topLeft, outerTopLeft, outerTopRight, topRight);
        }
        if (!hasPixel(skin, u, v + 1, uv)) {
            addQuad(quads, face.downNormal(), u, v, bottomRight, outerBottomRight, outerBottomLeft, bottomLeft);
        }
    }

    private static void addQuad(List<Quad> quads, Vector3f normal, int u, int v,
                                Vector3f first, Vector3f second, Vector3f third, Vector3f fourth) {
        Vertex[] vertices = new Vertex[]{
            new Vertex(first, (u + 1) / TEXTURE_SIZE, v / TEXTURE_SIZE),
            new Vertex(second, u / TEXTURE_SIZE, v / TEXTURE_SIZE),
            new Vertex(third, u / TEXTURE_SIZE, (v + 1) / TEXTURE_SIZE),
            new Vertex(fourth, (u + 1) / TEXTURE_SIZE, (v + 1) / TEXTURE_SIZE)
        };

        Vector3f edgeA = new Vector3f(vertices[1].position()).sub(vertices[0].position());
        Vector3f edgeB = new Vector3f(vertices[2].position()).sub(vertices[0].position());
        if (edgeA.cross(edgeB).dot(normal) < 0) {
            vertices = new Vertex[]{vertices[0], vertices[3], vertices[2], vertices[1]};
        }
        quads.add(new Quad(new Vector3f(normal), vertices));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasPixel(NativeImage skin, int u, int v, UVQuad bounds) {
        return u >= bounds.x1() && u < bounds.x2() && v >= bounds.y1() && v < bounds.y2() && hasPixel(skin, u, v);
    }

    private static boolean hasPixel(NativeImage skin, int u, int v) {
        if (u < 0 || v < 0 || u >= skin.getWidth() || v >= skin.getHeight()) {
            return false;
        }
        return (skin.getLuminanceOrAlpha(u, v) & 0xFF) != 0;
    }

    private static boolean isEmpty(UVQuad uv) {
        return uv == null || uv.x1() == uv.x2() || uv.y1() == uv.y2();
    }

    private record Quad(Vector3f normal, Vertex[] vertices) {
    }

    private record Vertex(float x, float y, float z, float u, float v) {
        private Vertex(Vector3f position, float u, float v) {
            this(position.x(), position.y(), position.z(), u, v);
        }

        private Vector3f position() {
            return new Vector3f(x, y, z);
        }
    }

    private record Face(Vector3f topRight, Vector3f topLeft, Vector3f bottomLeft, Vector3f bottomRight,
                        Vector3f normal) {
        private static Face of(BreastSide side, UVDirection direction) {
            float x1 = side.isLeft ? -4f : 0f;
            float x2 = side.isLeft ? 0f : 4f;
            float y1 = 0f;
            float y2 = 5f;
            float z1 = 0f;
            float z2 = 3f;

            return switch (direction) {
                case EAST -> new Face(
                    new Vector3f(x2, y1, z2),
                    new Vector3f(x2, y1, z1),
                    new Vector3f(x2, y2, z1),
                    new Vector3f(x2, y2, z2),
                    direction.getUnitVector()
                );
                case WEST -> new Face(
                    new Vector3f(x1, y1, z1),
                    new Vector3f(x1, y1, z2),
                    new Vector3f(x1, y2, z2),
                    new Vector3f(x1, y2, z1),
                    direction.getUnitVector()
                );
                case DOWN -> new Face(
                    new Vector3f(x2, y1, z2),
                    new Vector3f(x1, y1, z2),
                    new Vector3f(x1, y1, z1),
                    new Vector3f(x2, y1, z1),
                    direction.getUnitVector()
                );
                case UP -> new Face(
                    new Vector3f(x2, y2, z1),
                    new Vector3f(x1, y2, z1),
                    new Vector3f(x1, y2, z2),
                    new Vector3f(x2, y2, z2),
                    direction.getUnitVector()
                );
                case NORTH -> new Face(
                    new Vector3f(x2, y1, z1),
                    new Vector3f(x1, y1, z1),
                    new Vector3f(x1, y2, z1),
                    new Vector3f(x2, y2, z1),
                    direction.getUnitVector()
                );
            };
        }

        private Vector3f point(float s, float t) {
            Vector3f top = new Vector3f(topLeft).lerp(topRight, s);
            Vector3f bottom = new Vector3f(bottomLeft).lerp(bottomRight, s);
            return top.lerp(bottom, t);
        }

        private Vector3f rightNormal() {
            return new Vector3f(topRight).sub(topLeft).normalize();
        }

        private Vector3f leftNormal() {
            return rightNormal().negate();
        }

        private Vector3f downNormal() {
            return new Vector3f(bottomLeft).sub(topLeft).normalize();
        }

        private Vector3f upNormal() {
            return downNormal().negate();
        }
    }
}
