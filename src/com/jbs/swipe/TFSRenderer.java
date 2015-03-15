package com.jbs.swipe;

import org.sdeck.SDecKit;
import org.sdeck.renderer.AbstractRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.jbs.framework.control.ApplicationState;

public class TFSRenderer extends AbstractRenderer{

	private Game game;
	private ApplicationState state;
	private Camera camera;
	private SpriteBatch batch;
	private float[] projection;
	
	protected TFSRenderer(SDecKit kit, Game game, boolean stereo) {
		super(kit, stereo);
		this.game = game;
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public void setBatch(SpriteBatch batch) {
		this.batch = batch;
	}
	
	public void setProjection(float[] projection) {
		this.projection = projection;
	}
	
	public void setState(ApplicationState state) {
		this.state = state;
	}
	
	public void draw(Camera camera, SpriteBatch batch, ApplicationState state) {
		setCamera(camera);
		setBatch(batch);
		setState(state);
		camera.position.add(0, -1, -1);
		camera.lookAt(0, 0, 0);
		camera.rotateAround(new Vector3(0, 0, 0), new Vector3(0, 1, 0), 1f);
		super.onDrawFrame();
	}
	
	float r = 1f;
	@Override
	protected void onDrawScene(float[] projectionMatrix) {
		//batch.setProjectionMatrix(new Matrix4(projectionMatrix));
		
		//camera.rotateAround(new Vector3(0, 0, 0), new Vector3(0, 1, 0), r);
		state.renderTo(batch);
		
		//game.finishRenderingState(camera, batch);
//		r *= -1;
//		camera.rotateAround(new Vector3(0, 0, 0), new Vector3(0, 1, 0), r);
	}

	@Override
	protected float[] onSetupProjection() {
		return projection;
	}
}