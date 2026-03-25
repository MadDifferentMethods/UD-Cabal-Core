package UD.CabalCore.DeltaLayer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import UD.CabalCore.DeltaLayer.cache.MeshKey;
import UD.CabalCore.DeltaLayer.mesh.Baker;
import UD.CabalCore.DeltaLayer.mesh.Cache;
import UD.CabalCore.DeltaLayer.mesh.MeshBundle;
import UD.CabalCore.DeltaLayer.mesh.PartMesh;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class LayerRender<T extends AbstractClientPlayer, M extends PlayerModel<T>> extends RenderLayer<T, M> {

    public LayerRender(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        System.out.println("[DeltaLayer] render entered for " + player.getName().getString());

        if (!Decider.shouldRender(player)) {
            System.out.println("[DeltaLayer] render skipped by decider");
            return;
        }

        boolean slim = "slim".equals(player.getModelName());
        MeshKey key = new MeshKey(player.getSkinTextureLocation(), slim);

        System.out.println("[DeltaLayer] render passed decider");
        System.out.println("[DeltaLayer] skin = " + player.getSkinTextureLocation());
        System.out.println("[DeltaLayer] slim = " + slim);

        MeshBundle mesh = Cache.getOrBuild(key, () -> {
            System.out.println("[DeltaLayer] invoking baker for " + key);
            return Baker.bake(key);
        });

        System.out.println("[DeltaLayer] head quads = " + mesh.head.quads().size());
        System.out.println("[DeltaLayer] body quads = " + mesh.body.quads().size());
        System.out.println("[DeltaLayer] leftArm quads = " + mesh.leftArm.quads().size());
        System.out.println("[DeltaLayer] rightArm quads = " + mesh.rightArm.quads().size());
        System.out.println("[DeltaLayer] leftLeg quads = " + mesh.leftLeg.quads().size());
        System.out.println("[DeltaLayer] rightLeg quads = " + mesh.rightLeg.quads().size());

        if (mesh.head.isEmpty()
                && mesh.body.isEmpty()
                && mesh.leftArm.isEmpty()
                && mesh.rightArm.isEmpty()
                && mesh.leftLeg.isEmpty()
                && mesh.rightLeg.isEmpty()) {
            System.out.println("[DeltaLayer] mesh bundle is empty");
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation()));
        PlayerModel<T> model = this.getParentModel();

        boolean oldHat = model.hat.visible;
        boolean oldJacket = model.jacket.visible;
        boolean oldLeftSleeve = model.leftSleeve.visible;
        boolean oldRightSleeve = model.rightSleeve.visible;
        boolean oldLeftPants = model.leftPants.visible;
        boolean oldRightPants = model.rightPants.visible;

        model.hat.visible = false;
        model.jacket.visible = false;
        model.leftSleeve.visible = false;
        model.rightSleeve.visible = false;
        model.leftPants.visible = false;
        model.rightPants.visible = false;

        try {
            renderBoundPart(poseStack, consumer, packedLight, model.head, mesh.head);
            renderBoundPart(poseStack, consumer, packedLight, model.body, mesh.body);
            renderBoundPart(poseStack, consumer, packedLight, model.leftArm, mesh.leftArm);
            renderBoundPart(poseStack, consumer, packedLight, model.rightArm, mesh.rightArm);
            renderBoundPart(poseStack, consumer, packedLight, model.leftLeg, mesh.leftLeg);
            renderBoundPart(poseStack, consumer, packedLight, model.rightLeg, mesh.rightLeg);
        } finally {
            model.hat.visible = oldHat;
            model.jacket.visible = oldJacket;
            model.leftSleeve.visible = oldLeftSleeve;
            model.rightSleeve.visible = oldRightSleeve;
            model.leftPants.visible = oldLeftPants;
            model.rightPants.visible = oldRightPants;
        }
    }

    private void renderBoundPart(
            PoseStack poseStack,
            VertexConsumer consumer,
            int light,
            ModelPart modelPart,
            PartMesh mesh
    ) {
        if (mesh.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        modelPart.translateAndRotate(poseStack);
        Renderer.renderPart(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, mesh);
        poseStack.popPose();
    }
}