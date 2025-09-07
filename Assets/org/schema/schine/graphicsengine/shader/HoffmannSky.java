package org.schema.schine.graphicsengine.shader;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;

public class HoffmannSky implements Shaderable {

	public Vector3f Position;
	private float _sunIntensity = 1.0f; //def 1.0f
	private float _turbitity = 1.0f;
	private Vector3f _hGg = new Vector3f(0.9f, 0.9f, 0.9f);
	private float _inscatteringMultiplier = 1.0f;

	private float _betaRayMultiplier = 1.0f; //def 8.0f

	private float _betaMieMultiplier = 0.00005f; //def 0.00005f;

	private Vector3f _betaRPlusBetaM;
	private Vector3f _betaDashR;
	private Vector3f _betaDashM;
	private Vector3f _oneOverBetaRPlusBetaM;
	private Vector4f _multipliers;
	private Vector4f _sunColorAndIntensity;

	private Vector3f _betaRay;
	private Vector3f _betaDashRay;
	private Vector3f _betaMie;
	private Vector3f _betaDashMie;

	public HoffmannSky() {

		float n = 1.0003f;
		float N = 2.545e25f;
		float pn = 0.035f;

		float[] lambda = new float[3];
		float[] lambda2 = new float[3];
		float[] lambda4 = new float[3];

		lambda[0] = 1.0f / 650e-9f; // red
		lambda[1] = 1.0f / 570e-9f; // green
		lambda[2] = 1.0f / 475e-9f; // blue

		for (int i = 0; i < 3; ++i) {
			lambda2[i] = lambda[i] * lambda[i];
			lambda4[i] = lambda2[i] * lambda2[i];
		}

		Vector3f vLambda2 = new Vector3f(lambda2[0], lambda2[1], lambda2[2]);
		Vector3f vLambda4 = new Vector3f(lambda4[0], lambda4[1], lambda4[2]);

		// Rayleigh scattering constants

		float temp = (FastMath.PI * FastMath.PI * (n * n - 1.0f)
				* (n * n - 1.0f) * (6.0f + 3.0f * pn) / (6.0f - 7.0f * pn) / N);
		float beta = (8.0f * temp * FastMath.PI / 3f);
		_betaRay = new Vector3f(vLambda4);
		_betaRay.scale(beta);

		float betaDash = temp / 2.0f;
		_betaDashRay = new Vector3f(vLambda4);
		_betaDashRay.scale(betaDash);

		// Mie scattering constants

		float T = 2.0f;
		float c = (6.544f * T - 6.51f) * 1e-17f;
		float temp2 = (0.434f * c * (2.0f * FastMath.PI) * (2.0f * FastMath.PI) * 0.5f);

		_betaDashMie = new Vector3f(vLambda2);
		_betaDashMie.scale(temp2);

		float[] K = new float[]{0.685f, 0.679f, 0.670f};
		float temp3 = (0.434f * c * FastMath.PI * (2.0f * FastMath.PI) * (2.0f * FastMath.PI));

		Vector3f vBetaMieTemp = new Vector3f(K[0] * lambda2[0], K[1]
				* lambda2[1], K[2] * lambda2[2]);

		_betaMie = new Vector3f(vBetaMieTemp);
		_betaMie.scale(temp3);
	}

	private void computeAttenuation(float thetaS) {
		float beta = 0.04608365822050f * _turbitity - 0.04586025928522f;
		float tauR, tauA;
		float[] fTau = new float[3];
		float m = (1.0f / (FastMath.cos(thetaS) + 0.15f * FastMath.pow(
				97.885f - thetaS / FastMath.PI * 180.0f, -1.253f))); // Relative
		// Optical
		// Mass
		float[] lambda = new float[]{0.65f, 0.57f, 0.475f};

		for (int i = 0; i < 3; ++i) {
			// Rayleigh Scattering
			// lambda in um.
			tauR = (FastMath.exp(-m * 0.008735f
					* FastMath.pow(lambda[i], -4.08f)));

			// Aerosal (water + dust) attenuation
			// beta - amount of aerosols present
			// alpha - ratio of small to large particle sizes. (0:4,usually 1.3)
			float alpha = 1.3f;
			tauA = (FastMath.exp(-m * beta
					* FastMath.pow(lambda[i], -alpha))); // lambda should be in
			// um

			fTau[i] = tauR * tauA;
		}

		_sunColorAndIntensity = new Vector4f(fTau[0], fTau[1], fTau[2],
				_sunIntensity * 100.0f);

	}

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		Vector3f vZenith = new Vector3f(0.0f, 0.001f, 0.0f);
		//		Position = Controller.camera.getPosition().getVector3f();

		Position = AbstractScene.mainLight.getPos();
		float thetaS = Position.dot(vZenith);
		if (thetaS < -vZenith.y) {
			//			System.err.println("thetaS "+thetaS);
			thetaS = -1;
		}
		thetaS = (FastMath.acos(thetaS));

		computeAttenuation(thetaS);
		setMaterialProperties();

		//		GlUtil.updateShaderVector3f( shader, "lightPosition",
		//				AbstractScene.mainLight.getLightPos()[0],
		//				AbstractScene.mainLight.getLightPos()[1],
		//				AbstractScene.mainLight.getLightPos()[2]);
		GlUtil.updateShaderVector3f(shader, "betaDashM", _betaDashM);
		GlUtil.updateShaderVector3f(shader, "betaDashR", _betaDashR);
		GlUtil.updateShaderVector3f(shader, "betaRPlusBetaM", _betaRPlusBetaM);
		GlUtil.updateShaderVector3f(shader, "hGg", _hGg);
		GlUtil.updateShaderVector4f(shader, "multipliers", _multipliers);
		GlUtil.updateShaderVector3f(shader, "oneOverBetaRPlusBetaM", _oneOverBetaRPlusBetaM);
		GlUtil.updateShaderVector4f(shader, "sunColorAndIntensity", _sunColorAndIntensity);
		//		uniform vec3 betaDashM;
		//		uniform vec3 betaDashR;
		//		uniform vec3 betaRPlusBetaM;
		//		uniform vec3 hGg;
		//		uniform vec4 multipliers;
		//		uniform vec3 oneOverBetaRPlusBetaM;
		//		uniform vec4 sunColorAndIntensity;
		//		uniform vec3 sunDirection;
		//		uniform mat4 worldView;
		//		uniform mat4 worldViewProject;

	}

	private void setMaterialProperties() {
		float reflectance = 0.1f;

		Vector3f vecBetaR = new Vector3f(_betaRay);
		vecBetaR.scale(_betaRayMultiplier);

		_betaDashR = new Vector3f(_betaDashRay);
		_betaDashR.scale(_betaRayMultiplier);
		Vector3f vecBetaM = new Vector3f(_betaMie);
		vecBetaM.scale(_betaMieMultiplier);

		_betaDashM = new Vector3f(_betaDashMie);
		_betaDashM.scale(_betaMieMultiplier);

		_betaRPlusBetaM = new Vector3f(vecBetaR);
		_betaRPlusBetaM.add(vecBetaM);

		_oneOverBetaRPlusBetaM = new Vector3f(1.0f / _betaRPlusBetaM.x,
				1.0f / _betaRPlusBetaM.y, 1.0f / _betaRPlusBetaM.z);
		//		_hGg = new Vector3f(1.0f - _hGg.x * _hGg.x, 1.0f + _hGg.x
		//				* _hGg.x, 2.0f * _hGg.x);
		_multipliers = new Vector4f(_inscatteringMultiplier,
				0.138f * reflectance, 0.113f * reflectance, 0.08f * reflectance);

	}

	public String ToString() {
		return "Atmosphere";
	}
}
