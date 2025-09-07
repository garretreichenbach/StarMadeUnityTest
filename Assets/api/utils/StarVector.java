package api.utils;

import javax.vecmath.Vector3f;

public class StarVector {
    private Vector3f internalVector;
    public StarVector(float x, float y, float z){
        internalVector = new Vector3f(x,y,z);
    }
    public StarVector(Vector3f v){
        internalVector = v;
    }
    public StarVector multiply(float scale){
        internalVector.scale(scale);
        return this;
    }
    public StarVector add(StarVector v){
        internalVector.add(v.internalVector);
        return this;
    }
    public StarVector normalize(){
        internalVector.normalize();
        return this;
    }

    public Vector3f getInternalVector(){
        return internalVector;
    }
    public float length(){
        return internalVector.length();
    }
}
