package UD.CabalCore.DeltaLayer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import UD.CabalCore.DeltaLayer.mesh.BakedQuad3D;
import UD.CabalCore.DeltaLayer.mesh.PartMesh;

public final class Renderer {

    private Renderer() {
    }

    public static void renderPart(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, PartMesh mesh) {
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        Matrix3f normal = last.normal();

        for (BakedQuad3D quad : mesh.quads()) {
            put(consumer, pose, normal, light, overlay, quad, quad.v0);
            put(consumer, pose, normal, light, overlay, quad, quad.v1);
            put(consumer, pose, normal, light, overlay, quad, quad.v2);
            put(consumer, pose, normal, light, overlay, quad, quad.v3);
        }
    }

    private static void put(
            VertexConsumer consumer,
            Matrix4f pose,
            Matrix3f normal,
            int light,
            int overlay,
            BakedQuad3D quad,
            UD.CabalCore.DeltaLayer.mesh.Vertex3D v
    ) {
        consumer.vertex(pose, v.x, v.y, v.z)
                .color(255, 255, 255, 255)
                .uv(v.u, v.v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normal, quad.nx, quad.ny, quad.nz)
                .endVertex();
    }
}