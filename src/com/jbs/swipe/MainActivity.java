package com.jbs.swipe;

import org.sdeck.SDeConfig;
import org.sdeck.SDecKit;
import org.sdeck.stereo.Display;
import org.sdeck.stereo.Display.Layout;
import org.sdeck.stereo.Display.RenderMode;
import org.sdeck.stereo.Display.TnMode;
import org.sdeck.stereo.StereoDisplay;

import android.content.Context;
import android.content.Intent;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.ti.s3d.S3DView;

public class MainActivity extends AndroidApplication {
	
	private static final int
		VIRTUAL_WIDTH = 1024, VIRTUAL_HEIGHT = 768;
	
	private static final boolean
		USE_STEREOSCOPY = true;
	
	private static final String
		PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuyBJBU+LXFk2ZGLqSX5ZwamkSL3YaXxPcLtm9QeMCIj8vlXsQeBQLMOG1WGUR00fEMWxOX3W/rhzWGtHGNQlB2EfesWZARIuw+1ImzjlFMQuYPosdQKhvHPhOIcv5hBngrkl0zX2SZJOW0wr6ByW1tYIlSILJ7NK8m5AcPtYlo1nJSAlylb6g+oS9BzFPbMhRWk0JsYtEYoCWEnGE++betBApAUtLDwx4hKPrZAJtMJIRSZNuGegqmkyWOVrpf5xzE1Aw2n0MMlfNvFNaS8IXdn4GEPTcfE8GE2io60i+yFRkQ27VaF2m3ELxENu4MrCdnRTX8RJAYMcUoogGSTV8QIDAQAB";
	
	private GooglePlayBilling billingAPI;
	private SDecKit kit;
	private S3DView s3dView;
	
	private static final float
		NEAR_Z = 3.0f,
		FAR_Z = 30.0f,
		SCREEN_PLANE_Z = 10.0f,
		/** Field-of-view (In Degrees) */
		FOV = 45.0f,
		/** Interocular Distance : The distance between the viewer's eyes */
		IOD = 0.2f;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
        billingAPI = new GooglePlayBilling(this, PUBLIC_KEY);
        kit = createKit(this);
        
        // Will be initialized after the game is initialized.
        s3dView = null;
        Game game;
        
        if (USE_STEREOSCOPY)
	        game = new Game(billingAPI, VIRTUAL_WIDTH, VIRTUAL_HEIGHT) {
	        	Vector3 target = new Vector3(screenWidth()/2, screenHeight()/2, 0);
	        	float defaultIOD = 0f;
	        	boolean isRenderingLeft = false, isRenderingRight = false;
		    	@Override
		    	public void create() {
		    		super.create();
		    	}
		    	
		    	@Override
				public void beginRenderingState(Camera camera, SpriteBatch batch) {
		    		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		    		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		    		
		    		Vector3 target = new Vector3(screenWidth()/2, screenHeight()/2, 0);
		    		
		    		isRenderingRight = true;
		    		camera.translate(defaultIOD/2, 0, 0);
		    		//camera.lookAt(target.x, target.y, target.z);
		    		camera.update();
		    		batch.setProjectionMatrix(camera.combined);
		    		batch.begin();
		    		Gdx.gl.glViewport(screen().actualWidth()/2, 0, screen().actualWidth()/2, screen().actualHeight());
		    		applicationState().renderTo(batch);
		    		batch.end();
		    		isRenderingRight = false;
		    		
		    		isRenderingLeft = true;
		    		camera.translate(-defaultIOD, 0, 0);
		    		//camera.lookAt(target.x, target.y, target.z);
		    		camera.update();
		    		batch.setProjectionMatrix(camera.combined);
		    		batch.begin();
		    		Gdx.gl.glViewport(0, 0, screen().actualWidth()/2, screen().actualHeight());
		    		applicationState().renderTo(batch);
		    		batch.end();
		    		isRenderingLeft = false;
		    		
		    		camera.translate(defaultIOD/2, 0, 0);
		    	}
		    	@Override
		    	public void beginIODChange(SpriteBatch batch, float deltaIOD) {
		    		batch.end();
		    		
		    		if (isRenderingLeft)
		    			camera().translate(-deltaIOD/2, 0, 0);
		    		if (isRenderingRight)
		    			camera().translate(deltaIOD/2, 0, 0);
		    		//camera().lookAt(target.x, target.y, target.z);
		    		camera().update();
		    		batch.setProjectionMatrix(camera().combined);
		    		batch.begin();
		    	}
		    	@Override
		    	public void endIODChange(SpriteBatch batch, float deltaIOD) {
		    		batch.end();
		    		if (isRenderingLeft)
		    			camera().translate(deltaIOD/2, 0, 0);
		    		if (isRenderingRight)
		    			camera().translate(-deltaIOD/2, 0, 0);
		    		camera().lookAt(target.x, target.y, target.z);
		    		camera().update();
		    		
		    		batch.begin();
		    	}
		    };
		else
			game = new Game(billingAPI, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        initialize(game, cfg);
        
        if (USE_STEREOSCOPY)
	        // Now that the Game is initialized, initialize the s3dView
	        if (graphics.getView() instanceof SurfaceView) {
	        	SurfaceView view = (SurfaceView) graphics.getView();
	        	SurfaceHolder holder = view.getHolder();
	        	s3dView = new S3DView(holder);
	        	s3dView.setConfig(S3DView.Layout.SIDE_BY_SIDE_LR, S3DView.RenderMode.STEREO);
	        	s3dView.surfaceCreated(holder);
	        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult(" + requestCode + ", " + resultCode + ", " + data);
        
        // Pass on the activity result to the helper for handling
        if (!billingAPI.helper().handleActivityResult(requestCode, resultCode, data)) {
        	super.onActivityResult(requestCode, resultCode, data);
        } else {
        	System.out.println("onActivityResult handled by IABUtil.");
        }
    }
    
	private SDecKit createKit(Context context) {
		SDeConfig config = new SDeConfig.Builder(context.getApplicationContext())
								.setNearZ(NEAR_Z)
								.setFarZ(FAR_Z)
								.setScreenZ(SCREEN_PLANE_Z)
								.setFOV(FOV)
								.setIOD(IOD)
								.create();
		return new SDecKit(config);
	}
}