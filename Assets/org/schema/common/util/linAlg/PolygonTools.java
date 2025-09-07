package org.schema.common.util.linAlg;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;

public class PolygonTools {

	
	public static float distanceInst( Vector3f v1, Vector3f v2,Vector3f v3, Vector3f p )
	{
	    // prepare data    
	    Vector3f v21 = Vector3fTools.sub(v2, v1); Vector3f p1 = Vector3fTools.sub(p, v1);
	    Vector3f v32 = Vector3fTools.sub(v3, v2); Vector3f p2 = Vector3fTools.sub(p, v2);
	    Vector3f v13 = Vector3fTools.sub(v1, v3); Vector3f p3 = Vector3fTools.sub(p, v3);
	    Vector3f nor = Vector3fTools.crossProduct( v21, v13 );
	    return FastMath.carmackSqrt( // inside/outside test    
	                 (FastMath.sign(Vector3fTools.dot(Vector3fTools.crossProduct(v21,nor),p1)) + 
            		 FastMath.sign(Vector3fTools.dot(Vector3fTools.crossProduct(v32,nor),p2)) + 
            		 FastMath.sign(Vector3fTools.dot(Vector3fTools.crossProduct(v13,nor),p3))<2.0) 
	                  ?
	                  // 3 edges    
	                		  Math.min( Math.min( 
	                		  Vector3fTools.dot2( Vector3fTools.sub(Vector3fTools.mult(v21,FastMath.clamp(Vector3fTools.dot(v21,p1)/Vector3fTools.dot2(v21),0.0f,1.0f)), p1)), 
	                		  Vector3fTools.dot2( Vector3fTools.sub(Vector3fTools.mult(v32,FastMath.clamp(Vector3fTools.dot(v32,p2)/Vector3fTools.dot2(v32),0.0f,1.0f)), p2)) ), 
	                		  Vector3fTools.dot2( Vector3fTools.sub(Vector3fTools.mult(v13,FastMath.clamp(Vector3fTools.dot(v13,p3)/Vector3fTools.dot2(v13),0.0f,1.0f)), p3)) )
	                  :
	                  // 1 face    
	                	  Vector3fTools.dot(nor,p1)*Vector3fTools.dot(nor,p1)/Vector3fTools.dot2(nor) );
	}
	public static float distance( Vector3f v1, Vector3f v2,Vector3f v3, Vector3f p, PolygonToolsVars v )
	{
		// prepare data    
		v.v21.sub(v2, v1); 
		v.p1.sub(p, v1);
		
		v.v32.sub(v3, v2); 
		v.p2.sub(p, v2);
		
		v.v13.sub(v1, v3); 
		v.p3.sub(p, v3);
		v.nor.cross( v.v21, v.v13 );
		
		if(v.v13.lengthSquared() < 64 && v.v32.lengthSquared() < 64 && v.v21.lengthSquared() < 64 && Vector3fTools.lengthSquared(v1, p) > 100){
			//face is sufficiently small and far away. use aproximation
			return FastMath.carmackSqrt(Math.min(Math.min(Vector3fTools.lengthSquared(v1, p), Vector3fTools.lengthSquared(v2, p)), Vector3fTools.lengthSquared(v3, p)));
		}
		v.v21crossNor.cross(v.v21,v.nor);
		v.v32crossNor.cross(v.v32,v.nor);
		v.v13crossNor.cross(v.v13,v.nor);
		
		
		
		if( // inside/outside test    
				FastMath.sign(Vector3fTools.dot(v.v21crossNor,v.p1)) + 
						FastMath.sign(Vector3fTools.dot(v.v32crossNor,v.p2)) + 
						FastMath.sign(Vector3fTools.dot(v.v13crossNor,v.p3)) < 2.0 ){
			
			v.mult21.scale(FastMath.clamp(Vector3fTools.dot(v.v21, v.p1)/Vector3fTools.dot2(v.v21),0.0f,1.0f), v.v21 );
			v.mult32.scale(FastMath.clamp(Vector3fTools.dot(v.v32, v.p2)/Vector3fTools.dot2(v.v32),0.0f,1.0f), v.v32);
			v.mult13.scale(FastMath.clamp(Vector3fTools.dot(v.v13, v.p3)/Vector3fTools.dot2(v.v13),0.0f,1.0f), v.v13);
			
			// 3 edges    
			return FastMath.carmackSqrt(Math.min( Math.min( 
					Vector3fTools.dot2( Vector3fTools.sub(v.mult21, v.p1) ),
					Vector3fTools.dot2( Vector3fTools.sub(v.mult32, v.p2))), 
					Vector3fTools.dot2( Vector3fTools.sub(v.mult13, v.p3))));
		}else{
			// 1 face    
			
			return FastMath.carmackSqrt(Vector3fTools.dot(v.nor,v.p1)*Vector3fTools.dot(v.nor,v.p1)/Vector3fTools.dot2(v.nor)) ;
		}
							
							
	}
	
	public static float distance(Triangle[] tri, int iNumFaces, Vector3f point, PolygonToolsVars v) {
		float smallest = Float.POSITIVE_INFINITY;
		for (int i = 0; i < iNumFaces; i++) {
			Triangle t = tri[i];
			float dist = distance(t.v1, t.v2, t.v3, point, v);
			if(dist < smallest){
				smallest = dist;
				v.outFrom.set(t.v1);
				
				Vector3fTools.getCenterOfTriangle(t.v1, t.v2, t.v3, v.outFrom);
				
			}
			
		}
		return smallest;
	}
	public static float distanceBad(Triangle[] tri, int iNumFaces, Vector3f point) {
		float smallest = Float.POSITIVE_INFINITY;
		// D3DXVECTOR3 v1,v2, intersect, normal;
		// float dotp,dist,length;
		for (int i = 0; i < iNumFaces; i++) {
			// First, we have to find the normal of the polygon

			// We start to meake a vetor from the first and second vertices

			float tmpv1x = tri[i].v2.x - tri[i].v1.x;
			float tmpv1y = tri[i].v2.y - tri[i].v1.y;
			float tmpv1z = tri[i].v2.z - tri[i].v1.z;

			// Then with the first and third vertices

			float tmpv2x = tri[i].v3.x - tri[i].v1.x;
			float tmpv2y = tri[i].v3.y - tri[i].v1.y;
			float tmpv2z = tri[i].v3.z - tri[i].v1.z;

			// After, we do the cross product

			float tmpnormalx = tmpv1y * tmpv2z - tmpv1z * tmpv2y;
			float tmpnormaly = tmpv1z * tmpv2x - tmpv1x * tmpv2z;
			float tmpnormalz = tmpv1x * tmpv2y - tmpv1y * tmpv2x;

			// Now we have to make it a unit vector

			float length = FastMath
					.carmackSqrt(tmpnormalx * tmpnormalx + tmpnormaly * tmpnormaly + tmpnormalz * tmpnormalz);
			tmpnormalx = tmpnormalx / length;
			tmpnormaly = tmpnormaly / length;
			tmpnormalz = tmpnormalz / length;

			// Making the intersection point

			tmpv1x = point.x - tri[i].v1.x;
			tmpv1y = point.y - tri[i].v1.y;
			tmpv1z = point.z - tri[i].v1.z;
			float dotp = tmpv1x * tmpnormalx + tmpv1y * tmpnormaly + tmpv1z * tmpnormalz;
			// intersect.x = v1.x-normal.x*dotp;
			// intersect.y = v1.y-normal.y*dotp;
			// intersect.z = v1.z-normal.z*dotp;
			float tmpintersectx = -tmpnormalx * dotp;
			float tmpintersecty = -tmpnormaly * dotp;
			float tmpintersectz = -tmpnormalz * dotp;

			// Now that we got the intersection point,

			// we just have to compute the lenght of this vector

			float dist = FastMath.carmackSqrt(
					tmpintersectx * tmpintersectx + 
					tmpintersecty * tmpintersecty + 
					tmpintersectz * tmpintersectz);
			smallest = Math.min(dist, smallest);
		}
		return smallest;
	}
}
