package com.xamlo.core.engine.graphics.components;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.xamlo.core.engine.graphics.api.components.scene.IMovable;
import com.xamlo.core.engine.graphics.api.components.scene.IRenderable;
import com.xamlo.core.engine.graphics.api.components.scene.IRotatable;
import com.xamlo.core.engine.graphics.api.components.scene.IScalable;
import com.xamlo.core.engine.graphics.api.components.scene.IStretchable;


public abstract class AbstractRenderableObject implements IMovable, IRenderable, IRotatable, IScalable, IStretchable  {
	
	/**
	 * Матрица преобразования координат объекта в мировые
	 * У каждого объекта своя матрица и своё положение в мире
	 * 
	 * Положение объекта в пространстве, заданное матрицей 4х4
	 * Определяет положение XYZ, повороты Yaw, roll, pich и маштаб scale
	 * //TODO: исрпвить пример
	 * 
	 * {1.0, 0.0, 0.0, 0.0}
	 * {0.0, 1.0, 0.0, 0.0}
	 * {0.0, 0.0, 1.0, 0.0}
	 * {0.0, 0.0, 0.0, 1.0}
	 */
	protected final Matrix4f worldMatrix = new Matrix4f();

	/**
	 * Позиция камеры в мире
	 * [0] - x Coord
	 * [1] - y Coord
	 * [2] - z Coord
	 */
    protected Vector3f position;
	
	/**
	 * Поворот камеры в мире
	 * [0] - yaw
	 * [1] - pitch
	 * [2] - roll
	 */
	protected Vector3f rotation;
	
	/**
	 * Искажение геометрии объекта по Х У Z
	 */
	protected Vector3f geometryDeformation;
	
	/**
	 * Масштаб объекта
	 */
	protected float scale;

	
	public AbstractRenderableObject() {
		this.position = new Vector3f(0.0f, 0.0f, 0.0f);
		this.rotation = new Vector3f(0.0f, 0.0f, 0.0f);
		this.geometryDeformation = new Vector3f(1.0f, 1.0f, 1.0f);
		this.scale = 1.0f;
	}

	@Override
	public void move(Vector3f vector) {
		this.position = this.position.add(vector);		
	}
	
	@Override
	public void move(float xCoord, float yCoord, float zCoord) {
		this.move(new Vector3f(xCoord, yCoord, zCoord));
		
	}

	@Override
	public void rotate(Vector3f vector) {
		this.rotation = this.rotation.add(vector);

	}
	
	@Override
	public void rotate(float yaw, float pitch, float roll) {
		this.rotate(new Vector3f(yaw, pitch, roll));
	}

	@Override
	public void scale(float scaleIndex) {
		this.scale = scale*scaleIndex;
	}
	
	@Override
	public void setPosition(Vector3f pos) {
		this.position = pos;
	}
	
	@Override
	public void setPosition(float xCoord, float yCoord, float zCoord) {
		this.setPosition(new Vector3f(xCoord, yCoord, zCoord));
		
	}
	
	@Override
	public void setRotation(Vector3f rot) {
		this.rotation = rot;
	}
	
	@Override
	public void setRotation(float yaw, float pitch, float roll) {
		this.setRotation(new Vector3f(yaw, pitch, roll));
	}
	
	@Override
	public void setScale(float scaleIndex) {
		this.scale = scaleIndex;
	}

	@Override
	public void expandGeometry(Vector3f geometry) {
		this.geometryDeformation = this.geometryDeformation.add(geometry);		
	}

	@Override
	public void expandGeometry(float xCoord, float yCoord, float zCoord) {
		this.expandGeometry(new Vector3f(xCoord, yCoord, zCoord));
	}

	@Override
	public void setExpandGeometry(Vector3f geometry) {
		geometryDeformation = geometry;
	}

	@Override
	public void setExpandGeometry(float xCoord, float yCoord, float zCoord) {
		this.setExpandGeometry(new Vector3f(xCoord, yCoord, zCoord));
	}

	@Override
	public void resetGeometry() {
		geometryDeformation = new Vector3f(1.0f, 1.0f, 1.0f);
	}
	
	@Override
	public Vector3f getPosition() {
		return position;
	}

	@Override
	public Vector3f getRotation() {
		return rotation;
	}

	@Override
	public float getScale() {
		return scale;
	}
	
	/**
	 * Метод получения сетки объекта
	 * @return Mesh объекта
	 */
	public abstract GraphicalMesh getMesh();
	
	public abstract void loadMesh();


	/**
	 * @param offset - смещение по координатной оси
	 * @param rotation - поворот по трём осям вращения
	 * @param scale - масштаб объекта
	 * @return Матрица преобразования объекта в мировые координаты
	 */
    public Matrix4f getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        worldMatrix
        		//Получить единичную матрицу
		        //.identity()
		        .translate(offset)
        		//.rotateX((float)Math.toRadians(rotation.x))
                //.rotateY((float)Math.toRadians(rotation.y))
                //.rotateZ((float)Math.toRadians(rotation.z))
                .scale(geometryDeformation.mul(scale));
        return worldMatrix;
    }
    
	/**
	 * @param offset - смещение по координатной оси
	 * @param rotation - поворот по трём осям вращения
	 * @param scale - масштаб объекта
	 * @return Матрица преобразования объекта в мировые координаты
	 */
    public Matrix4f getWorldMatrix() {
        worldMatrix
		        .identity()
		        .translate(position)
                .scale(geometryDeformation.mul(scale));

        //.rotateX((float)Math.toRadians(rotation.x))
        //.rotateY((float)Math.toRadians(rotation.y))
        //.rotateZ((float)Math.toRadians(rotation.z))
        System.out.println("[MATRIX] geometryDef: " + geometryDeformation);
        System.out.println("[MATRIX] position: " + position);
        
		System.out.println("[MATRIX] updating world matrix for rot" + this.rotation.x + ", " + this.rotation.y + ", " + this.rotation.z);
		System.out.println("[MATRIX] {" + worldMatrix.m00() + ", " + worldMatrix.m01() + ", " + worldMatrix.m02() + ", " + worldMatrix.m03() + "}");
		System.out.println("[MATRIX] {" + worldMatrix.m10() + ", " + worldMatrix.m11() + ", " + worldMatrix.m12() + ", " + worldMatrix.m13() + "}");
		System.out.println("[MATRIX] {" + worldMatrix.m20() + ", " + worldMatrix.m21() + ", " + worldMatrix.m22() + ", " + worldMatrix.m23() + "}");
		System.out.println("[MATRIX] {" + worldMatrix.m30() + ", " + worldMatrix.m31() + ", " + worldMatrix.m32() + ", " + worldMatrix.m33() + "}");

        return worldMatrix;
    }
    
}
