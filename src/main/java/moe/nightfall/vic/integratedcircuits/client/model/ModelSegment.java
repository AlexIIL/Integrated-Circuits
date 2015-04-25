package moe.nightfall.vic.integratedcircuits.client.model;

import moe.nightfall.vic.integratedcircuits.client.Resources;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;

public class ModelSegment implements IComponentModel {
	private static CCModel model = generateModel();

	@Override
	public void renderModel(Transformation t) {
		model.copy().apply(t)
			.computeLighting(LightModel.standardLightModel)
			.render(new IconTransformation(Resources.ICON_IC_SOCKET));
	}

	private static CCModel generateModel() {
		CCModel m1 = CCModel.quadModel(120);
		m1.generateBlock(0, 3 / 16F, 0, 2 / 16F, 13 / 16F, 1 / 16D, 14 / 16F);
		m1.generateBlock(24, 2 / 16F, 0, 1 / 16F, 3 / 16F, 3 / 16D, 15 / 16F);
		m1.generateBlock(48, 13 / 16F, 0, 1 / 16F, 14 / 16F, 3 / 16D, 15 / 16F);
		m1.generateBlock(72, 3 / 16F, 0, 1 / 16F, 13 / 16F, 3 / 16D, 2 / 16F);
		m1.generateBlock(96, 3 / 16F, 0, 14 / 16F, 13 / 16F, 3 / 16D, 15 / 16F);
		m1.computeNormals();
		return m1;
	}
}