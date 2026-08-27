package com.amc.window;

import android.content.Context;

// OpenGL related variable
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES32;


// Events related packages
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;

// packages for java native I/O
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

// package for matrix manipulation
import android.opengl.Matrix;

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener,OnDoubleTapListener 
{
	private Context context;  // OpenCV la ha context lagato
	private GestureDetector gestureDetector;
	private int ShaderProgramObject;
	private int mvpMatrixUniform;

	private int vao_pyramid[]=new int[1];
	private int vao_cube[]=new int[1];

	private int vbo_position_pyramid[]=new int[1]; 
	private int vbo_position_cube[]=new int[1]; 

	private int vbo_color_pyramid[]=new int[1]; 
	private int vbo_color_cube[] = new int[1];

	private float angle_pyramid = 0.0f; 
	private float angle_cube = 0.0f;

	private float perspectiveProjectionMatrix[]=new float[16]; 

	public GLESView(Context _context) 
	{
		super(_context);
		context = _context;   // recommanded

		// Initialization of OpenGLES
		setEGLContextClientVersion(3); 
		setRenderer(this); 
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);  

		// Get GestureDetector
		gestureDetector= new GestureDetector(context,this,null,false);

		// set this class as  DoubleTapListener
		gestureDetector.setOnDoubleTapListener(this);
	}

	// 3 implementable method of glsurfaceview.renederer interface
	@Override
	public void onSurfaceCreated(GL10 gl,EGLConfig config)// GL10 i.e 1.0 OpenGL android vr suru zal tevha tyach 1st version hot 1.0 
	{
		// 	Code
		//int iResult=initialize(GL10);
	int iResult = initialize();


		if(iResult!=0)
		{
			System.out.println("AMC : initialize()failed");
			System.exit(0);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl,int width,int height)
	{
		// 	Code
		resize(width,height);
	}

	@Override
	public void onDrawFrame(GL10 gl)
	{
		// 	Code
		display();
		update();
	}

	// implement one method from view
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		// Code
		if(!gestureDetector.onTouchEvent(e))
		{
			super.onTouchEvent(e);
			return true;
		}

		return true;
	}

	// 3 methos from OnDoubleTapListener
	@Override
	public boolean onDoubleTap(MotionEvent e)
	{
		// Code
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		// Code
		return true;	
	}
				
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e)
	{
		// Code
		return true;
	}

	// 6 methods from Gesture OnGestureListener
	@Override
	public boolean onDown(MotionEvent e)
	{
		// Code
		//setText("Down");
		return true;
	}

	@Override
	public boolean onFling(MotionEvent even,MotionEvent e2,float velocityX,float velocityY)
	{
		// Code
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		// Code
	}

	@Override
	public boolean onScroll(MotionEvent even,MotionEvent e2,float distanceX,float distanceY)
	{
		// Code
		uninitialize();
		System.exit(0);
		return true;	
	}
			
	@Override
	public void onShowPress(MotionEvent e)
	{
		// Code
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		// Code
		return true;
	}

	// our custom openGLEs method
	private int initialize()
	{
		// code
		printGLESInfo();

		// vertex shader //
		final String vertexShaderSourceCode=String.format
		(
			"#version 320 es\n"+
			"in vec4 aPosition;\n"+
			"in vec4 aColor;\n" +
			"uniform mat4 uMVPMatrix;\n"+
			"out vec4 out_color;\n" +
			"void main(void)\n"+
			"{\n" +
			"gl_Position=aPosition;\n" +
			"out_color=aColor;\n" +
			"gl_Position=uMVPMatrix*aPosition;\n" +
			"}\n"
		);

		int vertexShaderObject=GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER); // GLES32 is class not namespace  
		GLES32.glShaderSource(vertexShaderObject,vertexShaderSourceCode);
		GLES32.glCompileShader(vertexShaderObject);

		int SHADER_COMPILE_STATUS[] = new int[1];
		int infoLogLength[] = new int[1];
		String szInfoLog=null;

		// error check 
		GLES32.glGetShaderiv(vertexShaderObject,GLES32.GL_COMPILE_STATUS,SHADER_COMPILE_STATUS,0);

		if(SHADER_COMPILE_STATUS[0]==GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(vertexShaderObject,GLES32.GL_INFO_LOG_LENGTH,infoLogLength,0);
				
			if(infoLogLength[0]>0)
			{
				szInfoLog=GLES32.glGetShaderInfoLog(vertexShaderObject);
				System.out.println("amc: vertexShaderCompilationLog"+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}

		// Fragment shader //
		final String fragmentShaderSourceCode=String.format
		(
			"#version 320 es\n"+
			"precision highp float;\n"+
			"in vec4 out_color;\n" +
			"out vec4 FragColor;\n"+
			"void main(void)\n"+
			"{FragColor=out_color;\n"+
			"}\n"
		);

		int fragmentShaderObject=GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER); // GLES32 he class aahe namespace nahi 
		GLES32.glShaderSource(fragmentShaderObject,fragmentShaderSourceCode);
		GLES32.glCompileShader(fragmentShaderObject);
	
		SHADER_COMPILE_STATUS[0] = 0;
		infoLogLength[0] = 0;
		szInfoLog=null;

		// error check 
		GLES32.glGetShaderiv(fragmentShaderObject,GLES32.GL_COMPILE_STATUS,SHADER_COMPILE_STATUS,0);

		if(SHADER_COMPILE_STATUS[0]==GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(fragmentShaderObject,GLES32.GL_INFO_LOG_LENGTH,infoLogLength,0);
			
			if(infoLogLength[0]>0)
			{
				szInfoLog=GLES32.glGetShaderInfoLog(fragmentShaderObject);
				System.out.println("amc: fragmentShaderCompilationLog"+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}

		// create , attach ,link shader program object
		ShaderProgramObject=GLES32.glCreateProgram();
		GLES32.glAttachShader(ShaderProgramObject, vertexShaderObject);
		GLES32.glAttachShader(ShaderProgramObject, fragmentShaderObject);

		// Bind shader attributes at the certain index in shader to same index in host program
		GLES32.glBindAttribLocation(ShaderProgramObject,MyAttributes.AMC_ATTRIBUTE_POSITION,"aPosition");
	    GLES32.glBindAttribLocation(ShaderProgramObject,MyAttributes.AMC_ATTRIBUTE_COLOR,"aColor");

		GLES32.glLinkProgram(ShaderProgramObject);

		int ShaderProgramLinkStatus[] = new int[1];
		infoLogLength[0] = 0;
		szInfoLog=null;

		GLES32.glGetProgramiv(ShaderProgramObject,GLES32.GL_LINK_STATUS,ShaderProgramLinkStatus,0);
		if(ShaderProgramLinkStatus[0]==GLES32.GL_FALSE)
		{
			GLES32.glGetProgramiv(ShaderProgramObject,GLES32.GL_INFO_LOG_LENGTH,infoLogLength,0);
			if(infoLogLength[0]>0)
			{
				GLES32.glGetProgramInfoLog(ShaderProgramObject);
				//szInfoLog = GLES32.glGetProgramInfoLog(ShaderProgramObject);
				System.out.println("amc: ShaderProgramLinkLog = "+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}

		// get the required uniform location from the shader 
		mvpMatrixUniform=GLES32.glGetUniformLocation(ShaderProgramObject, "uMVPMatrix");
			
		// Provide vertex position,color,normal,texcord
		final float pyramid_position[] =new float[]
		{
			// front
			 0.0f,  1.0f,  0.0f, // front-top
			-1.0f, -1.0f,  1.0f, // front-left
			 1.0f, -1.0f,  1.0f, // front-right

			 // right
			 0.0f,  1.0f,  0.0f, // right-top
			 1.0f, -1.0f,  1.0f, // right-left
			 1.0f, -1.0f, -1.0f, // right-right

			 // back
			 0.0f,  1.0f,  0.0f, // back-top
			 1.0f, -1.0f, -1.0f, // back-left
			-1.0f, -1.0f, -1.0f, // back-right

			// left
			0.0f,  1.0f,  0.0f, // left-top
		   -1.0f, -1.0f, -1.0f, // left-left
		   -1.0f, -1.0f,  1.0f, // left-right
		};

		final float cube_position[] = new float[]
		{
			// FRONT
			 1.0f,  1.0f,  1.0f,
			-1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,

			 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,
			 1.0f, -1.0f,  1.0f,

			// RIGHT
			 1.0f,  1.0f, -1.0f,
			 1.0f,  1.0f,  1.0f,
			 1.0f, -1.0f,  1.0f,

			 1.0f,  1.0f, -1.0f,
			 1.0f, -1.0f,  1.0f,
			 1.0f, -1.0f, -1.0f,

			// BACK
			 1.0f,  1.0f, -1.0f,
			-1.0f,  1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,

			 1.0f,  1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			 1.0f, -1.0f, -1.0f,

			// LEFT
			-1.0f,  1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,

			-1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f,  1.0f,

			// TOP
			 1.0f,  1.0f, -1.0f,
			-1.0f,  1.0f, -1.0f,
			-1.0f,  1.0f,  1.0f,

			 1.0f,  1.0f, -1.0f,
			-1.0f,  1.0f,  1.0f,
			 1.0f,  1.0f,  1.0f,

			// BOTTOM
			 1.0f, -1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f,

			 1.0f, -1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f,
			 1.0f, -1.0f, -1.0f,
		};

		final float pyramid_color[] =new float[]
		{
			// front
			1.0f, 0.0f, 0.0f, // front-top
			0.0f, 1.0f, 0.0f, // front-left
			0.0f, 0.0f, 1.0f, // front-right

			// right
			1.0f, 0.0f, 0.0f, // right-top
			0.0f, 0.0f, 1.0f, // right-left
			0.0f, 1.0f, 0.0f, // right-right

			// back
			1.0f, 0.0f, 0.0f, // back-top
			0.0f, 1.0f, 0.0f, // back-left
			0.0f, 0.0f, 1.0f, // back-right

			// left
			1.0f, 0.0f, 0.0f, // left-top
			0.0f, 0.0f, 1.0f, // left-left
			0.0f, 1.0f, 0.0f, // left-right
		};

		final float cube_color[] = new float[]
		{
						// front
			1.0f, 0.0f, 0.0f, // top-right of front
			1.0f, 0.0f, 0.0f, // top-left of front
			1.0f, 0.0f, 0.0f, // bottom-left of front
			1.0f, 0.0f, 0.0f, // bottom-right of front

			// right
			0.0f, 0.0f, 1.0f, // top-right of right
			0.0f, 0.0f, 1.0f, // top-left of right
			0.0f, 0.0f, 1.0f, // bottom-left of right
			0.0f, 0.0f, 1.0f, // bottom-right of right

			// back
			1.0f, 1.0f, 0.0f, // top-right of back
			1.0f, 1.0f, 0.0f, // top-left of back
			1.0f, 1.0f, 0.0f, // bottom-left of back
			1.0f, 1.0f, 0.0f, // bottom-right of back

			// left
			1.0f, 0.0f, 1.0f, // top-right of left
			1.0f, 0.0f, 1.0f, // top-left of left
			1.0f, 0.0f, 1.0f, // bottom-left of left
			1.0f, 0.0f, 1.0f, // bottom-right of left

			// top
			0.0f, 1.0f, 0.0f, // top-right of top
			0.0f, 1.0f, 0.0f, // top-left of top
			0.0f, 1.0f, 0.0f, // bottom-left of top
			0.0f, 1.0f, 0.0f, // bottom-right of top

			// bottom
			1.0f, 0.5f, 0.0f, // top-right of bottom
			1.0f, 0.5f, 0.0f, // top-left of bottom
			1.0f, 0.5f, 0.0f, // bottom-left of bottom
			1.0f, 0.5f, 0.0f, // bottom-right of bottom
		};

		// vao pyramid
		GLES32.glGenVertexArrays(1,vao_pyramid,0); // vao=vertex array object
		GLES32.glBindVertexArray(vao_pyramid[0]);

		// vbo_position pyramid 
		GLES32.glGenBuffers(1, vbo_position_pyramid,0);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_position_pyramid[0]);

		// step 3 create a native buffer suitable for native i/o but for java
		ByteBuffer byteBuffer=ByteBuffer.allocateDirect(pyramid_position.length*4);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer pyramidPositionBuffer=byteBuffer.asFloatBuffer();
		pyramidPositionBuffer.put(pyramid_position);
		pyramidPositionBuffer.position(0);

		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,pyramid_position.length*4, pyramidPositionBuffer, GLES32.GL_STATIC_DRAW);
		GLES32.glVertexAttribPointer(MyAttributes.AMC_ATTRIBUTE_POSITION, 3,GLES32.GL_FLOAT, false, 0, 0);
		GLES32.glEnableVertexAttribArray(MyAttributes.AMC_ATTRIBUTE_POSITION);

		// pyramid COLOR VBO
		GLES32.glGenBuffers(1, vbo_color_pyramid, 0);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_color_pyramid[0]);

		ByteBuffer pByteBuffer = ByteBuffer.allocateDirect(pyramid_color.length * 4);
		pByteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer colorBuffer = pByteBuffer.asFloatBuffer();
		colorBuffer.put(pyramid_color);
		colorBuffer.position(0);

		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,pyramid_color.length * 4,colorBuffer,GLES32.GL_STATIC_DRAW);
		GLES32.glVertexAttribPointer(MyAttributes.AMC_ATTRIBUTE_COLOR,3,GLES32.GL_FLOAT,false,0,0);
		GLES32.glEnableVertexAttribArray(MyAttributes.AMC_ATTRIBUTE_COLOR);
		
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
		GLES32.glBindVertexArray(0);

		// vao cube
		GLES32.glGenVertexArrays(1,vao_cube,0); // vao=vertex array object
		GLES32.glBindVertexArray(vao_cube[0]);

		// vbo_position cube 
		GLES32.glGenBuffers(1, vbo_position_cube,0);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_position_cube[0]);

		// step 3 create a native buffer suitable for native i/o but for java
		ByteBuffer cPosByteBuffer = ByteBuffer.allocateDirect(cube_position.length * 4);
		cPosByteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer cubePositionBuffer = cPosByteBuffer.asFloatBuffer();

		cubePositionBuffer.put(cube_position);
		cubePositionBuffer.position(0);

		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,cube_position.length*4, cubePositionBuffer, GLES32.GL_STATIC_DRAW);
		GLES32.glVertexAttribPointer(MyAttributes.AMC_ATTRIBUTE_POSITION, 3,GLES32.GL_FLOAT, false, 0, 0);
		GLES32.glEnableVertexAttribArray(MyAttributes.AMC_ATTRIBUTE_POSITION);

		// cube color vbo
		GLES32.glGenBuffers(1, vbo_color_cube, 0);
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_color_cube[0]);

		ByteBuffer cByteBuffer = ByteBuffer.allocateDirect(cube_color.length * 4);
		cByteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer cubeColorBuffer = cByteBuffer.asFloatBuffer();
		cubeColorBuffer.put(cube_color);
		cubeColorBuffer.position(0);

		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,cube_color.length * 4,cubeColorBuffer,GLES32.GL_STATIC_DRAW);
		GLES32.glVertexAttribPointer(MyAttributes.AMC_ATTRIBUTE_COLOR,3,GLES32.GL_FLOAT,false,0,0);
		GLES32.glEnableVertexAttribArray(MyAttributes.AMC_ATTRIBUTE_COLOR);
		
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
		GLES32.glBindVertexArray(0);

		// Depth initialization
		GLES32.glClearDepthf(1.0f);
		GLES32.glEnable(GLES32.GL_DEPTH_TEST);
		GLES32.glDepthFunc(GLES32.GL_LEQUAL);

		// Set the clear color
		GLES32.glClearColor(0.0f,0.0f,0.0f,1.0f);

		Matrix.setIdentityM(perspectiveProjectionMatrix,0);
    
		return 0;
	}

	//private void printGLESInfo(GL10 gl)
	private void printGLESInfo()
	{
		// code
		String gles_vendor = GLES32.glGetString(GLES32.GL_VENDOR);
		String gles_renderer = GLES32.glGetString(GLES32.GL_RENDERER);
		String gles_version = GLES32.glGetString(GLES32.GL_VERSION);
		String gles_glsl = GLES32.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);	
	}

	private void resize(int width, int height)
	{
		if (height <= 0)
			height = 1;

		GLES32.glViewport(0, 0, width, height);

		Matrix.perspectiveM(perspectiveProjectionMatrix,0,45.0f,(float) width / (float) height,	0.1f,100.0f	);
	}

	private void display()
	{
		GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

		GLES32.glUseProgram(ShaderProgramObject);

		float modelMatrix[] = new float[16];
		float viewMatrix[] = new float[16];
		float modelViewMatrix[] = new float[16];
		float modelViewProjectionMatrix[] = new float[16];
		float translationMatrix[] = new float[16];
		float rotationMatrix[] = new float[16];
				
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.setIdentityM(viewMatrix, 0);
		Matrix.setIdentityM(modelViewMatrix, 0);
		Matrix.setIdentityM(modelViewProjectionMatrix, 0);
		Matrix.setIdentityM(translationMatrix, 0);
		Matrix.setIdentityM(rotationMatrix, 0);

		Matrix.translateM(translationMatrix, 0, -1.5f, 0.0f, -6.0f);
		Matrix.setRotateM(rotationMatrix, 0, angle_pyramid, 0.0f, 1.0f, 0.0f);

		Matrix.multiplyMM(modelViewMatrix, 0, translationMatrix, 0, rotationMatrix, 0);
		Matrix.multiplyMM(modelViewProjectionMatrix, 0, perspectiveProjectionMatrix, 0, modelViewMatrix, 0);

		GLES32.glUniformMatrix4fv(mvpMatrixUniform, 1, false, modelViewProjectionMatrix, 0);

		GLES32.glBindVertexArray(vao_pyramid[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 12);
		GLES32.glBindVertexArray(0);

		// cube
		Matrix.translateM(viewMatrix, 0, 1.5f, 0.0f, -6.0f);
		Matrix.rotateM(rotationMatrix, 0, angle_cube, 1.0f, 1.0f, 1.0f);

		Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0);
		Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.multiplyMM(modelViewProjectionMatrix, 0,perspectiveProjectionMatrix, 0,modelViewMatrix, 0);

		GLES32.glUniformMatrix4fv(mvpMatrixUniform, 1, false,modelViewProjectionMatrix, 0);
		GLES32.glBindVertexArray(vao_cube[0]);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 36); 
		GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);
		requestRender();
	}

	private void update()
	{
		// code
		angle_pyramid = angle_pyramid + 2.0f;
		if (angle_pyramid >= 360.0f)
		{
			angle_pyramid = angle_pyramid - 360.0f;
		}

		angle_cube = angle_cube + 1.0f;
		if (angle_cube >= 360.0f)
		{
			angle_cube -= 360.0f;
		}
	}

	private void uninitialize()
	{
		// code
		if (vbo_color_cube[0]>0)
		{
			GLES32.glDeleteBuffers(1, vbo_color_cube,0);
			vbo_color_cube[0]= 0;
		}

		//free vbo postion
		if (vbo_position_cube[0]>0)
		{
			GLES32.glDeleteBuffers(1, vbo_position_cube,0);
			vbo_position_cube[0]= 0;
		}

		//free vao
		if (vao_cube[0]>0)
		{
			GLES32.glDeleteVertexArrays(1, vao_cube,0); 
			vao_cube[0] = 0;
		}

		if (vbo_color_pyramid[0]>0)
		{
			GLES32.glDeleteBuffers(1, vbo_color_pyramid,0);
			vbo_color_pyramid[0]= 0;
		}

		//free vbo 
		if (vbo_position_pyramid[0]>0)
		{
			GLES32.glDeleteBuffers(1, vbo_position_pyramid,0);
			vbo_position_pyramid[0]= 0;
		}

		//free vao
		if (vao_pyramid[0]>0)
		{
			GLES32.glDeleteVertexArrays(1, vao_pyramid,0); 
			vao_pyramid[0] = 0;
		}

		// Detach,delete shader objects and delete shader program object
		if (ShaderProgramObject>0)
		{
			GLES32.glUseProgram(ShaderProgramObject);
			int retVal[]=new int[1];

			GLES32.glGetProgramiv(ShaderProgramObject, GLES32.GL_ATTACHED_SHADERS,retVal,0);
			int numAttachedShaders=retVal[0];

			if (numAttachedShaders > 0)
			{
				int shaderObjects[]=new int[numAttachedShaders];
				GLES32.glGetAttachedShaders(ShaderProgramObject,numAttachedShaders,retVal,0,shaderObjects,0);

				for (int i = 0; i < numAttachedShaders; i++)
				{
					GLES32.glDetachShader(ShaderProgramObject, shaderObjects[i]);
					GLES32.glDeleteShader(shaderObjects[i]);
					shaderObjects[i] = 0;
				}
			}
			//free(pShaders);
		}

		GLES32.glUseProgram(0);

		GLES32.glDeleteProgram(ShaderProgramObject);

		ShaderProgramObject = 0;
	}
}







