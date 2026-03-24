package UD.CabalCore.DeltaLayer.mesh;

import com.mojang.blaze3d.platform.NativeImage;
import UD.CabalCore.DeltaLayer.cache.ImgCache;
import UD.CabalCore.DeltaLayer.cache.MeshKey;

import java.util.ArrayList;
import java.util.List;

public final class Baker {

    private static final float BASE_DEPTH = 0.42F;

    private Baker() {
    }

    public static MeshBundle bake(MeshKey key) {
        NativeImage image = ImgCache.get(key.skin());

        if (image == null) {
            return empty();
        }

        if (image.getWidth() != 64 || image.getHeight() != 64) {
            return empty();
        }

        boolean slim = key.slim();

        return new MeshBundle(
                bakeCuboid(image, PartDefinitions.HEAD),
                bakeCuboid(image, PartDefinitions.BODY),
                bakeCuboid(image, PartDefinitions.leftArm(slim)),
                bakeCuboid(image, PartDefinitions.rightArm(slim)),
                bakeCuboid(image, PartDefinitions.LEFT_LEG),
                bakeCuboid(image, PartDefinitions.RIGHT_LEG)
        );
    }

    private static MeshBundle empty() {
        PartMesh empty = new PartMesh(List.of());
        return new MeshBundle(empty, empty, empty, empty, empty, empty);
    }

    private static float partScale(PartDefinition def) {
        if (def == PartDefinitions.HEAD) {
            return 1.75F;
        }
        if (def == PartDefinitions.BODY) {
            return 1.00F;
        }
        return 0.95F;
    }

    private static float faceScaleFrontBack(PartDefinition def) {
        if (def == PartDefinitions.HEAD) {
            return 1.10F;
        }
        return 1.00F;
    }

    private static float faceScaleSide(PartDefinition def) {
        if (def == PartDefinitions.HEAD) {
            return 1.15F;
        }
        return 0.85F;
    }

    private static float faceScaleTopBottom(PartDefinition def) {
        if (def == PartDefinitions.HEAD) {
            return 0.95F;
        }
        return 0.70F;
    }

    private static PartMesh bakeCuboid(NativeImage image, PartDefinition def) {
        List<BakedQuad3D> quads = new ArrayList<>();

        float inflate = def.inflate;

        float minX = def.minX - inflate;
        float maxX = def.maxX() + inflate;
        float minY = def.minY - inflate;
        float maxY = def.maxY() + inflate;
        float minZ = def.minZ - inflate;
        float maxZ = def.maxZ() + inflate;

        int u = def.u0;
        int v = def.v0;
        int w = def.width;
        int h = def.height;
        int d = def.depth;

        emitFront(image, quads, def, u + d,         v + d,     w, h, minX, maxX, minY, maxY, minZ);
        emitBack(image, quads,  def, u + d + w + d, v + d,     w, h, minX, maxX, minY, maxY, maxZ);
        emitLeft(image, quads,  def, u,             v + d,     d, h, minX, minY, maxY, minZ, maxZ);
        emitRight(image, quads, def, u + d + w,     v + d,     d, h, maxX, minY, maxY, minZ, maxZ);
        emitTop(image, quads,   def, u + d,         v,         w, d, minX, maxX, minY, minZ, maxZ);
        emitBottom(image, quads,def, u + d + w,     v,         w, d, minX, maxX, maxY, minZ, maxZ);

        return new PartMesh(quads);
    }

    private static void emitFront(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int w, int h,
            float minX, float maxX, float minY, float maxY, float zShell
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleFrontBack(def);
        float zOuter = zShell - depth;

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                if (!opaque(image, u0 + px, v0 + py)) continue;

                boolean leftOpen   = px == 0     || !opaque(image, u0 + px - 1, v0 + py);
                boolean rightOpen  = px == w - 1 || !opaque(image, u0 + px + 1, v0 + py);
                boolean topOpen    = py == 0     || !opaque(image, u0 + px, v0 + py - 1);
                boolean bottomOpen = py == h - 1 || !opaque(image, u0 + px, v0 + py + 1);

                float x0 = lerp(px,     0, w, minX, maxX);
                float x1 = lerp(px + 1, 0, w, minX, maxX);
                float y0 = lerp(py,     0, h, minY, maxY);
                float y1 = lerp(py + 1, 0, h, minY, maxY);

                if (def == PartDefinitions.HEAD && py == 0) {
                    y0 -= 0.60F;
                } else if (py == 0) {
                    y0 -= 0.0005F;
                }

                addQuadFront(quads, x0, x1, y0, y1, zOuter, u0 + px, v0 + py);

                if (leftOpen) {
                    addQuadLeft(quads, x0, y0, y1, zShell, zOuter, u0 + px, v0 + py);
                }
                if (rightOpen) {
                    addQuadRight(quads, x1, y0, y1, zShell, zOuter, u0 + px, v0 + py);
                }
                if (topOpen) {
                    addQuadTop(quads, x0, x1, y0, zShell, zOuter, u0 + px, v0 + py);
                }
                if (bottomOpen) {
                    addQuadBottom(quads, x0, x1, y1, zShell, zOuter, u0 + px, v0 + py);
                }
            }
        }
    }

    private static void emitBack(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int w, int h,
            float minX, float maxX, float minY, float maxY, float zShell
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleFrontBack(def);
        float zOuter = zShell + depth;

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                if (!opaque(image, u0 + px, v0 + py)) continue;

                boolean leftOpen   = px == 0     || !opaque(image, u0 + px - 1, v0 + py);
                boolean rightOpen  = px == w - 1 || !opaque(image, u0 + px + 1, v0 + py);
                boolean topOpen    = py == 0     || !opaque(image, u0 + px, v0 + py - 1);
                boolean bottomOpen = py == h - 1 || !opaque(image, u0 + px, v0 + py + 1);

                float x0 = lerp(px,     0, w, maxX, minX);
                float x1 = lerp(px + 1, 0, w, maxX, minX);
                float y0 = lerp(py,     0, h, minY, maxY);
                float y1 = lerp(py + 1, 0, h, minY, maxY);

                if (def == PartDefinitions.HEAD && py == 0) {
                    y0 -= 0.60F;
                } else if (py == 0) {
                    y0 -= 0.0005F;
                }

                addQuadBack(quads, x0, x1, y0, y1, zOuter, u0 + px, v0 + py);

                if (leftOpen) {
                    addQuadRight(quads, x0, y0, y1, zOuter, zShell, u0 + px, v0 + py);
                }
                if (rightOpen) {
                    addQuadLeft(quads, x1, y0, y1, zOuter, zShell, u0 + px, v0 + py);
                }
                if (topOpen) {
                    addQuadTop(quads, x0, x1, y0, zOuter, zShell, u0 + px, v0 + py);
                }
                if (bottomOpen) {
                    addQuadBottom(quads, x0, x1, y1, zOuter, zShell, u0 + px, v0 + py);
                }
            }
        }
    }

    private static void emitLeft(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int d, int h,
            float xShell, float minY, float maxY, float minZ, float maxZ
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleSide(def);
        float xOuter = xShell - depth;

        for (int py = 0; py < h; py++) {
            for (int pz = 0; pz < d; pz++) {
                if (!opaque(image, u0 + pz, v0 + py)) continue;

                boolean frontOpen  = pz == 0     || !opaque(image, u0 + pz - 1, v0 + py);
                boolean backOpen   = pz == d - 1 || !opaque(image, u0 + pz + 1, v0 + py);
                boolean topOpen    = py == 0     || !opaque(image, u0 + pz, v0 + py - 1);
                boolean bottomOpen = py == h - 1 || !opaque(image, u0 + pz, v0 + py + 1);

                float z0 = lerp(pz,     0, d, maxZ, minZ);
                float z1 = lerp(pz + 1, 0, d, maxZ, minZ);
                float y0 = lerp(py,     0, h, minY, maxY);
                float y1 = lerp(py + 1, 0, h, minY, maxY);

                if (def == PartDefinitions.HEAD && py == 0) {
                    float lift = 0.60F;

                    // left face: frontmost column is the last one
                    if (pz == d - 1) {
                        lift = 0.18F;
                    }

                    y0 -= lift;
                }

                addQuadLeftFace(quads, xOuter, y0, y1, z0, z1, u0 + pz, v0 + py);

                if (frontOpen) {
                    addQuadFront(quads, xOuter, xShell, y0, y1, z0, u0 + pz, v0 + py);
                }
                if (backOpen) {
                    addQuadBack(quads, xOuter, xShell, y0, y1, z1, u0 + pz, v0 + py);
                }
                if (topOpen) {
                    addQuadTopX(quads, xOuter, xShell, y0, z0, z1, u0 + pz, v0 + py);
                }
                if (bottomOpen) {
                    addQuadBottomX(quads, xOuter, xShell, y1, z0, z1, u0 + pz, v0 + py);
                }
            }
        }
    }

    private static void emitRight(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int d, int h,
            float xShell, float minY, float maxY, float minZ, float maxZ
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleSide(def);
        float xOuter = xShell + depth;

        for (int py = 0; py < h; py++) {
            for (int pz = 0; pz < d; pz++) {
                if (!opaque(image, u0 + pz, v0 + py)) continue;

                boolean frontOpen  = pz == 0     || !opaque(image, u0 + pz - 1, v0 + py);
                boolean backOpen   = pz == d - 1 || !opaque(image, u0 + pz + 1, v0 + py);
                boolean topOpen    = py == 0     || !opaque(image, u0 + pz, v0 + py - 1);
                boolean bottomOpen = py == h - 1 || !opaque(image, u0 + pz, v0 + py + 1);

                float z0 = lerp(pz,     0, d, minZ, maxZ);
                float z1 = lerp(pz + 1, 0, d, minZ, maxZ);
                float y0 = lerp(py,     0, h, minY, maxY);
                float y1 = lerp(py + 1, 0, h, minY, maxY);

                if (def == PartDefinitions.HEAD && py == 0) {
                    float lift = 0.60F;

                    // right face: frontmost column is the first one
                    if (pz == 0) {
                        lift = 0.18F;
                    }

                    y0 -= lift;
                }

                addQuadRightFace(quads, xOuter, y0, y1, z0, z1, u0 + pz, v0 + py);

                if (frontOpen) {
                    addQuadFront(quads, xShell, xOuter, y0, y1, z0, u0 + pz, v0 + py);
                }
                if (backOpen) {
                    addQuadBack(quads, xShell, xOuter, y0, y1, z1, u0 + pz, v0 + py);
                }
                if (topOpen) {
                    addQuadTopX(quads, xShell, xOuter, y0, z0, z1, u0 + pz, v0 + py);
                }
                if (bottomOpen) {
                    addQuadBottomX(quads, xShell, xOuter, y1, z0, z1, u0 + pz, v0 + py);
                }
            }
        }
    }

    private static void emitTop(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int w, int d,
            float minX, float maxX, float yShell, float minZ, float maxZ
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleTopBottom(def) * 1.15F;
        float yOuter = yShell - depth - 0.0005F;

        for (int pz = 0; pz < d; pz++) {
            for (int px = 0; px < w; px++) {
                if (!opaque(image, u0 + px, v0 + pz)) continue;

                boolean leftOpen  = px == 0     || !opaque(image, u0 + px - 1, v0 + pz);
                boolean rightOpen = px == w - 1 || !opaque(image, u0 + px + 1, v0 + pz);
                boolean frontOpen = pz == 0     || !opaque(image, u0 + px, v0 + pz - 1);
                boolean backOpen  = pz == d - 1 || !opaque(image, u0 + px, v0 + pz + 1);

                float x0 = lerp(px,     0, w, minX, maxX);
                float x1 = lerp(px + 1, 0, w, minX, maxX);
                float z0 = lerp(pz,     0, d, maxZ, minZ);
                float z1 = lerp(pz + 1, 0, d, maxZ, minZ);

                addQuadTopFace(quads, x0, x1, yOuter, z0, z1, u0 + px, v0 + pz);

                if (leftOpen) {
                    addQuadLeft(quads, x0, yOuter, yShell, z0, z1, u0 + px, v0 + pz);
                }
                if (rightOpen) {
                    addQuadRight(quads, x1, yOuter, yShell, z0, z1, u0 + px, v0 + pz);
                }
                if (frontOpen) {
                    addQuadFrontZ(quads, x0, x1, yOuter, yShell, z0, u0 + px, v0 + pz);
                }
                if (backOpen) {
                    addQuadBackZ(quads, x0, x1, yOuter, yShell, z1, u0 + px, v0 + pz);
                }
            }
        }
    }

    private static void emitBottom(
            NativeImage image, List<BakedQuad3D> quads, PartDefinition def,
            int u0, int v0, int w, int d,
            float minX, float maxX, float yShell, float minZ, float maxZ
    ) {
        float depth = BASE_DEPTH * partScale(def) * faceScaleTopBottom(def);
        float yOuter = yShell + depth;

        for (int pz = 0; pz < d; pz++) {
            for (int px = 0; px < w; px++) {
                if (!opaque(image, u0 + px, v0 + pz)) continue;

                boolean leftOpen  = px == 0     || !opaque(image, u0 + px - 1, v0 + pz);
                boolean rightOpen = px == w - 1 || !opaque(image, u0 + px + 1, v0 + pz);
                boolean frontOpen = pz == 0     || !opaque(image, u0 + px, v0 + pz - 1);
                boolean backOpen  = pz == d - 1 || !opaque(image, u0 + px, v0 + pz + 1);

                float x0 = lerp(px,     0, w, minX, maxX);
                float x1 = lerp(px + 1, 0, w, minX, maxX);
                float z0 = lerp(pz,     0, d, maxZ, minZ);
                float z1 = lerp(pz + 1, 0, d, maxZ, minZ);

                addQuadBottomFace(quads, x0, x1, yOuter, z0, z1, u0 + px, v0 + pz);

                if (leftOpen) {
                    addQuadLeft(quads, x0, yShell, yOuter, z0, z1, u0 + px, v0 + pz);
                }
                if (rightOpen) {
                    addQuadRight(quads, x1, yShell, yOuter, z0, z1, u0 + px, v0 + pz);
                }
                if (frontOpen) {
                    addQuadFrontZ(quads, x0, x1, yShell, yOuter, z0, u0 + px, v0 + pz);
                }
                if (backOpen) {
                    addQuadBackZ(quads, x0, x1, yShell, yOuter, z1, u0 + px, v0 + pz);
                }
            }
        }
    }

    private static void addQuadFront(List<BakedQuad3D> quads, float x0, float x1, float y0, float y1, float z, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x0, y0, z, u, v),
                vtx(x1, y0, z, u + 1, v),
                vtx(x1, y1, z, u + 1, v + 1),
                vtx(x0, y1, z, u, v + 1),
                0.0F, 0.0F, -1.0F
        ));
    }

    private static void addQuadBack(List<BakedQuad3D> quads, float x0, float x1, float y0, float y1, float z, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x0, y0, z, u, v),
                vtx(x1, y0, z, u + 1, v),
                vtx(x1, y1, z, u + 1, v + 1),
                vtx(x0, y1, z, u, v + 1),
                0.0F, 0.0F, 1.0F
        ));
    }

    private static void addQuadLeft(List<BakedQuad3D> quads, float x, float y0, float y1, float z0, float z1, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x, y0, z0, u, v),
                vtx(x, y0, z1, u + 1, v),
                vtx(x, y1, z1, u + 1, v + 1),
                vtx(x, y1, z0, u, v + 1),
                -1.0F, 0.0F, 0.0F
        ));
    }

    private static void addQuadRight(List<BakedQuad3D> quads, float x, float y0, float y1, float z0, float z1, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x, y0, z0, u, v),
                vtx(x, y0, z1, u + 1, v),
                vtx(x, y1, z1, u + 1, v + 1),
                vtx(x, y1, z0, u, v + 1),
                1.0F, 0.0F, 0.0F
        ));
    }

    private static void addQuadTop(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x0, y, z0, u, v),
                vtx(x1, y, z0, u + 1, v),
                vtx(x1, y, z1, u + 1, v + 1),
                vtx(x0, y, z1, u, v + 1),
                0.0F, -1.0F, 0.0F
        ));
    }

    private static void addQuadBottom(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        quads.add(new BakedQuad3D(
                vtx(x0, y, z0, u, v),
                vtx(x1, y, z0, u + 1, v),
                vtx(x1, y, z1, u + 1, v + 1),
                vtx(x0, y, z1, u, v + 1),
                0.0F, 1.0F, 0.0F
        ));
    }

    private static void addQuadLeftFace(List<BakedQuad3D> quads, float x, float y0, float y1, float z0, float z1, int u, int v) {
        addQuadLeft(quads, x, y0, y1, z0, z1, u, v);
    }

    private static void addQuadRightFace(List<BakedQuad3D> quads, float x, float y0, float y1, float z0, float z1, int u, int v) {
        addQuadRight(quads, x, y0, y1, z0, z1, u, v);
    }

    private static void addQuadTopFace(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        addQuadTop(quads, x0, x1, y, z0, z1, u, v);
    }

    private static void addQuadBottomFace(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        addQuadBottom(quads, x0, x1, y, z0, z1, u, v);
    }

    private static void addQuadFrontZ(List<BakedQuad3D> quads, float x0, float x1, float y0, float y1, float z, int u, int v) {
        addQuadFront(quads, x0, x1, y0, y1, z, u, v);
    }

    private static void addQuadBackZ(List<BakedQuad3D> quads, float x0, float x1, float y0, float y1, float z, int u, int v) {
        addQuadBack(quads, x0, x1, y0, y1, z, u, v);
    }

    private static void addQuadTopX(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        addQuadTop(quads, x0, x1, y, z0, z1, u, v);
    }

    private static void addQuadBottomX(List<BakedQuad3D> quads, float x0, float x1, float y, float z0, float z1, int u, int v) {
        addQuadBottom(quads, x0, x1, y, z0, z1, u, v);
    }

    private static Vertex3D vtx(float x, float y, float z, int u, int v) {
        return new Vertex3D(x / 16.0F, y / 16.0F, z / 16.0F, u / 64.0F, v / 64.0F);
    }

    private static boolean opaque(NativeImage image, int x, int y) {
        int argb = image.getPixelRGBA(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        return alpha > 0;
    }

    private static float lerp(float value, float minIn, float maxIn, float minOut, float maxOut) {
        if (maxIn == minIn) {
            return minOut;
        }
        return minOut + (value - minIn) * (maxOut - minOut) / (maxIn - minIn);
    }
}