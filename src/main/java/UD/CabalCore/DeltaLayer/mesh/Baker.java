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
                bakeHead(image),
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
        if (def == PartDefinitions.BODY) {
            return 1.00F;
        }
        return 0.95F;
    }

    private static float faceScaleFrontBack(PartDefinition def) {
        return 1.00F;
    }

    private static float faceScaleSide(PartDefinition def) {
        return 0.85F;
    }

    private static float faceScaleTopBottom(PartDefinition def) {
        return 0.70F;
    }

    // =========================================================
// HEAD - 9th Rewrite with fresh set of eyes....
// =========================================================

    private static final float HEAD_FRONT_DEPTH   = 0.5f;
    private static final float HEAD_FRONT_UP    = 0.08f;
    private static final float HEAD_FRONT_DOWN  = 0.08f;
    private static final float HEAD_FRONT_WIDTH_EXTRA  = 0.10f;
    private static final float HEAD_FRONT_HEIGHT_EXTRA = 0.06f;
    private static final float HEAD_FRONT_VERTICAL_EXTRA= 0.50f;

    private static final float HEAD_SIDE_DEPTH    = 0.5f;
    private static final float HEAD_SIDE_UP     = 0.12f;
    private static final float HEAD_SIDE_DOWN   = 0.08f;

    private static final float HEAD_BACK_DEPTH    = 0.8f;
    private static final float HEAD_BACK_UP     = 0.08f;
    private static final float HEAD_BACK_DOWN   = 0.08f;
    private static final float HEAD_BACK_WIDTH_EXTRA   = 0.10f;
    private static final float HEAD_BACK_HEIGHT_EXTRA  = 0.06f;
    private static final float HEAD_BACK_VERTICAL_EXTRA = 0.50f;

    private static final float HEAD_TOP_DEPTH     = 0.5f;
    private static final float HEAD_TOP_WIDTH_EXTRA    = 0.10f;
    private static final float HEAD_TOP_DEPTH_EXTRA    = 0.10f;

    private static final float HEAD_BOTTOM_DEPTH  = 0.5f;

    private static final float EPS = 0.0001f;

    private static PartMesh bakeHead(NativeImage skin) {
        PartDefinition part = PartDefinitions.HEAD;

        // IMPORTANT:
        // Anchor to the real head cube so the 3D layer touches the head.
        float x0 = part.minX;
        float x1 = part.maxX();
        float y0 = part.minY;
        float y1 = part.maxY();
        float z0 = part.minZ;
        float z1 = part.maxZ();

        float px = (x1 - x0) / 8.0f;
        float py = (y1 - y0) / 8.0f;
        float pz = (z1 - z0) / 8.0f;

        List<HeadBox> boxes = new ArrayList<>(512);

        float frontMinX = x0 - HEAD_SIDE_DEPTH - HEAD_FRONT_WIDTH_EXTRA;
        float frontMaxX = x1 + HEAD_SIDE_DEPTH + HEAD_FRONT_WIDTH_EXTRA;

// Push the whole front projection slightly farther vertically.
        float frontMinY = y0 - HEAD_FRONT_UP - HEAD_FRONT_HEIGHT_EXTRA - HEAD_FRONT_VERTICAL_EXTRA;
        float frontMaxY = y1 + HEAD_FRONT_DOWN + HEAD_FRONT_HEIGHT_EXTRA + HEAD_FRONT_VERTICAL_EXTRA;

        float frontPx = (frontMaxX - frontMinX) / 8.0f;
        float frontPy = (frontMaxY - frontMinY) / 8.0f;

// FRONT: uv 40,8
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 40 + u, 8 + v)) continue;

                float ax0 = frontMinX + u * frontPx;
                float ax1 = ax0 + frontPx;
                float ay0 = frontMinY + v * frontPy;
                float ay1 = ay0 + frontPy;

                boxes.add(new HeadBox(
                        ax0, ax1,
                        ay0, ay1,
                        z0 - HEAD_FRONT_DEPTH, z0,
                        40 + u, 8 + v
                ));
            }
        }

        float backMinX = x0 - HEAD_SIDE_DEPTH - HEAD_BACK_WIDTH_EXTRA;
        float backMaxX = x1 + HEAD_SIDE_DEPTH + HEAD_BACK_WIDTH_EXTRA;

        float backMinY = y0 - HEAD_BACK_UP - HEAD_BACK_HEIGHT_EXTRA - HEAD_BACK_VERTICAL_EXTRA;
        float backMaxY = y1 + HEAD_BACK_DOWN + HEAD_BACK_HEIGHT_EXTRA + HEAD_BACK_VERTICAL_EXTRA;

        float backPx = (backMaxX - backMinX) / 8.0f;
        float backPy = (backMaxY - backMinY) / 8.0f;

// BACK: uv 56,8
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 56 + u, 8 + v)) continue;

                float ax0 = backMaxX - (u + 1) * backPx;
                float ax1 = backMaxX - u * backPx;
                float ay0 = backMinY + v * backPy;
                float ay1 = ay0 + backPy;

                boxes.add(new HeadBox(
                        ax0, ax1,
                        ay0, ay1,
                        z1, z1 + HEAD_BACK_DEPTH,
                        56 + u, 8 + v
                ));
            }
        }

        // LEFT: uv 32,8
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 32 + u, 8 + v)) continue;

                float az0 = z1 - (u + 1) * pz;
                float az1 = z1 - u * pz;
                float ay0 = y0 - HEAD_SIDE_UP + v * py;
                float ay1 = ay0 + py;

                boxes.add(new HeadBox(
                        x0 - HEAD_SIDE_DEPTH, x0,
                        ay0, ay1,
                        az0, az1,
                        32 + u, 8 + v
                ));
            }
        }

        // RIGHT: uv 48,8
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 48 + u, 8 + v)) continue;

                float az0 = z0 + u * pz;
                float az1 = az0 + pz;
                float ay0 = y0 - HEAD_SIDE_UP + v * py;
                float ay1 = ay0 + py;

                boxes.add(new HeadBox(
                        x1, x1 + HEAD_SIDE_DEPTH,
                        ay0, ay1,
                        az0, az1,
                        48 + u, 8 + v
                ));
            }
        }

        float topMinX = x0 - HEAD_SIDE_DEPTH - HEAD_TOP_WIDTH_EXTRA;
        float topMaxX = x1 + HEAD_SIDE_DEPTH + HEAD_TOP_WIDTH_EXTRA;

        float topMinZ = z0 - HEAD_FRONT_DEPTH - HEAD_TOP_DEPTH_EXTRA;
        float topMaxZ = z1 + HEAD_BACK_DEPTH + HEAD_TOP_DEPTH_EXTRA;

        float topPx = (topMaxX - topMinX) / 8.0f;
        float topPz = (topMaxZ - topMinZ) / 8.0f;

// TOP: uv 40,0
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 40 + u, v)) continue;

                float ax0 = topMinX + u * topPx;
                float ax1 = ax0 + topPx;
                float az0 = topMaxZ - (v + 1) * topPz;
                float az1 = topMaxZ - v * topPz;

                boxes.add(new HeadBox(
                        ax0, ax1,
                        y0 - HEAD_TOP_DEPTH, y0,
                        az0, az1,
                        40 + u, v
                ));
            }
        }

        // BOTTOM: uv 48,0
        for (int v = 0; v < 8; v++) {
            for (int u = 0; u < 8; u++) {
                if (!opaque(skin, 48 + u, v)) continue;

                float ax0 = x0 + u * px;
                float ax1 = ax0 + px;
                float az0 = z0 + v * pz;
                float az1 = az0 + pz;

                boxes.add(new HeadBox(
                        ax0, ax1,
                        y1, y1 + HEAD_BOTTOM_DEPTH,
                        az0, az1,
                        48 + u, v
                ));
            }
        }

        addHeadSeamBridges(boxes, skin, x0, x1, y0, y1, z0, z1, px, py, pz);

        List<BakedQuad3D> quads = new ArrayList<>(boxes.size() * 4);

        for (int i = 0; i < boxes.size(); i++) {
            HeadBox box = boxes.get(i);

            if (!coveredByNeighbor(boxes, i, FaceDir.NEG_Z)) {
                addQuadFront(quads, box.minX, box.maxX, box.minY, box.maxY, box.minZ, box.u, box.v);
            }
            if (!coveredByNeighbor(boxes, i, FaceDir.POS_Z)) {
                addQuadBack(quads, box.minX, box.maxX, box.minY, box.maxY, box.maxZ, box.u, box.v);
            }
            if (!coveredByNeighbor(boxes, i, FaceDir.NEG_X)) {
                addQuadLeft(quads, box.minX, box.minY, box.maxY, box.minZ, box.maxZ, box.u, box.v);
            }
            if (!coveredByNeighbor(boxes, i, FaceDir.POS_X)) {
                addQuadRight(quads, box.maxX, box.minY, box.maxY, box.minZ, box.maxZ, box.u, box.v);
            }
            if (!coveredByNeighbor(boxes, i, FaceDir.NEG_Y)) {
                addQuadTop(quads, box.minX, box.maxX, box.minY, box.minZ, box.maxZ, box.u, box.v);
            }
            if (!coveredByNeighbor(boxes, i, FaceDir.POS_Y)) {
                addQuadBottom(quads, box.minX, box.maxX, box.maxY, box.minZ, box.maxZ, box.u, box.v);
            }
        }

        return new PartMesh(quads);
    }

    private static void addHeadSeamBridges(List<HeadBox> boxes, NativeImage skin,
                                           float x0, float x1, float y0, float y1, float z0, float z1,
                                           float px, float py, float pz) {

        // FRONT <-> LEFT
        for (int row = 0; row < 8; row++) {
            if (opaque(skin, 40, 8 + row) && opaque(skin, 32 + 7, 8 + row)) {
                float ay0 = y0 - Math.max(HEAD_FRONT_UP, HEAD_SIDE_UP) + row * py;
                float ay1 = ay0 + py;
                boxes.add(new HeadBox(
                        x0 - HEAD_SIDE_DEPTH, x0,
                        ay0, ay1,
                        z0 - HEAD_FRONT_DEPTH, z0,
                        40, 8 + row
                ));
            }
        }

        // FRONT <-> RIGHT
        for (int row = 0; row < 8; row++) {
            if (opaque(skin, 40 + 7, 8 + row) && opaque(skin, 48, 8 + row)) {
                float ay0 = y0 - Math.max(HEAD_FRONT_UP, HEAD_SIDE_UP) + row * py;
                float ay1 = ay0 + py;
                boxes.add(new HeadBox(
                        x1, x1 + HEAD_SIDE_DEPTH,
                        ay0, ay1,
                        z0 - HEAD_FRONT_DEPTH, z0,
                        47, 8 + row
                ));
            }
        }

        // BACK <-> LEFT
        for (int row = 0; row < 8; row++) {
            if (opaque(skin, 56 + 7, 8 + row) && opaque(skin, 32, 8 + row)) {
                float ay0 = y0 - Math.max(HEAD_BACK_UP, HEAD_SIDE_UP) + row * py;
                float ay1 = ay0 + py;
                boxes.add(new HeadBox(
                        x0 - HEAD_SIDE_DEPTH, x0,
                        ay0, ay1,
                        z1, z1 + HEAD_BACK_DEPTH,
                        63, 8 + row
                ));
            }
        }

        // BACK <-> RIGHT
        for (int row = 0; row < 8; row++) {
            if (opaque(skin, 56, 8 + row) && opaque(skin, 48 + 7, 8 + row)) {
                float ay0 = y0 - Math.max(HEAD_BACK_UP, HEAD_SIDE_UP) + row * py;
                float ay1 = ay0 + py;
                boxes.add(new HeadBox(
                        x1, x1 + HEAD_SIDE_DEPTH,
                        ay0, ay1,
                        z1, z1 + HEAD_BACK_DEPTH,
                        56, 8 + row
                ));
            }
        }

        // TOP <-> FRONT
        for (int col = 0; col < 8; col++) {
            if (opaque(skin, 40 + col, 7) && opaque(skin, 40 + col, 8)) {
                float ax0 = x0 + col * px;
                float ax1 = ax0 + px;
                boxes.add(new HeadBox(
                        ax0, ax1,
                        y0 - HEAD_TOP_DEPTH, y0,
                        z0 - HEAD_FRONT_DEPTH, z0,
                        40 + col, 8
                ));
            }
        }

        // TOP <-> BACK
        for (int col = 0; col < 8; col++) {
            if (opaque(skin, 40 + col, 0) && opaque(skin, 56 + (7 - col), 8)) {
                float ax0 = x0 + col * px;
                float ax1 = ax0 + px;
                boxes.add(new HeadBox(
                        ax0, ax1,
                        y0 - HEAD_TOP_DEPTH, y0,
                        z1, z1 + HEAD_BACK_DEPTH,
                        40 + col, 0
                ));
            }
        }

        // BOTTOM <-> FRONT
        for (int col = 0; col < 8; col++) {
            if (opaque(skin, 48 + col, 0) && opaque(skin, 40 + col, 15)) {
                float ax0 = x0 + col * px;
                float ax1 = ax0 + px;
                boxes.add(new HeadBox(
                        ax0, ax1,
                        y1, y1 + HEAD_BOTTOM_DEPTH,
                        z0 - HEAD_FRONT_DEPTH, z0,
                        48 + col, 0
                ));
            }
        }

        // BOTTOM <-> BACK
        for (int col = 0; col < 8; col++) {
            if (opaque(skin, 48 + col, 7) && opaque(skin, 56 + (7 - col), 15)) {
                float ax0 = x0 + col * px;
                float ax1 = ax0 + px;
                boxes.add(new HeadBox(
                        ax0, ax1,
                        y1, y1 + HEAD_BOTTOM_DEPTH,
                        z1, z1 + HEAD_BACK_DEPTH,
                        48 + col, 7
                ));
            }
        }

        // TOP <-> LEFT
        for (int i = 0; i < 8; i++) {
            if (opaque(skin, 40, i) && opaque(skin, 32 + i, 8)) {
                float az0 = z1 - (i + 1) * pz;
                float az1 = z1 - i * pz;
                boxes.add(new HeadBox(
                        x0 - HEAD_SIDE_DEPTH, x0,
                        y0 - HEAD_TOP_DEPTH, y0,
                        az0, az1,
                        40, i
                ));
            }
        }

        // TOP <-> RIGHT
        for (int i = 0; i < 8; i++) {
            if (opaque(skin, 47, i) && opaque(skin, 48 + (7 - i), 8)) {
                float az0 = z1 - (i + 1) * pz;
                float az1 = z1 - i * pz;
                boxes.add(new HeadBox(
                        x1, x1 + HEAD_SIDE_DEPTH,
                        y0 - HEAD_TOP_DEPTH, y0,
                        az0, az1,
                        47, i
                ));
            }
        }

        // BOTTOM <-> LEFT
        for (int i = 0; i < 8; i++) {
            if (opaque(skin, 48, i) && opaque(skin, 32 + (7 - i), 15)) {
                float az0 = z0 + i * pz;
                float az1 = az0 + pz;
                boxes.add(new HeadBox(
                        x0 - HEAD_SIDE_DEPTH, x0,
                        y1, y1 + HEAD_BOTTOM_DEPTH,
                        az0, az1,
                        48, i
                ));
            }
        }

        // BOTTOM <-> RIGHT
        for (int i = 0; i < 8; i++) {
            if (opaque(skin, 55, i) && opaque(skin, 48 + i, 15)) {
                float az0 = z0 + i * pz;
                float az1 = az0 + pz;
                boxes.add(new HeadBox(
                        x1, x1 + HEAD_SIDE_DEPTH,
                        y1, y1 + HEAD_BOTTOM_DEPTH,
                        az0, az1,
                        55, i
                ));
            }
        }
    }

    private enum FaceDir {
        NEG_X, POS_X,
        NEG_Y, POS_Y,
        NEG_Z, POS_Z
    }

    private static boolean coveredByNeighbor(List<HeadBox> boxes, int selfIndex, FaceDir dir) {
        HeadBox a = boxes.get(selfIndex);

        for (int i = 0; i < boxes.size(); i++) {
            if (i == selfIndex) continue;
            HeadBox b = boxes.get(i);

            switch (dir) {
                case NEG_X -> {
                    if (Math.abs(b.maxX - a.minX) < EPS &&
                            overlaps(b.minY, b.maxY, a.minY, a.maxY) &&
                            overlaps(b.minZ, b.maxZ, a.minZ, a.maxZ)) {
                        return true;
                    }
                }
                case POS_X -> {
                    if (Math.abs(b.minX - a.maxX) < EPS &&
                            overlaps(b.minY, b.maxY, a.minY, a.maxY) &&
                            overlaps(b.minZ, b.maxZ, a.minZ, a.maxZ)) {
                        return true;
                    }
                }
                case NEG_Y -> {
                    if (Math.abs(b.maxY - a.minY) < EPS &&
                            overlaps(b.minX, b.maxX, a.minX, a.maxX) &&
                            overlaps(b.minZ, b.maxZ, a.minZ, a.maxZ)) {
                        return true;
                    }
                }
                case POS_Y -> {
                    if (Math.abs(b.minY - a.maxY) < EPS &&
                            overlaps(b.minX, b.maxX, a.minX, a.maxX) &&
                            overlaps(b.minZ, b.maxZ, a.minZ, a.maxZ)) {
                        return true;
                    }
                }
                case NEG_Z -> {
                    if (Math.abs(b.maxZ - a.minZ) < EPS &&
                            overlaps(b.minX, b.maxX, a.minX, a.maxX) &&
                            overlaps(b.minY, b.maxY, a.minY, a.maxY)) {
                        return true;
                    }
                }
                case POS_Z -> {
                    if (Math.abs(b.minZ - a.maxZ) < EPS &&
                            overlaps(b.minX, b.maxX, a.minX, a.maxX) &&
                            overlaps(b.minY, b.maxY, a.minY, a.maxY)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean overlaps(float a0, float a1, float b0, float b1) {
        return Math.min(a1, b1) - Math.max(a0, b0) > EPS;
    }

    private static final class HeadBox {
        final float minX, maxX;
        final float minY, maxY;
        final float minZ, maxZ;
        final int u, v;

        HeadBox(float minX, float maxX,
                float minY, float maxY,
                float minZ, float maxZ,
                int u, int v) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.u = u;
            this.v = v;
        }
    }

    // =========================================================
    // ORIGINAL IDEA BAKER FOR BODY / ARMS / LEGS
    // =========================================================

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

                if (py == 0) {
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

                if (py == 0) {
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