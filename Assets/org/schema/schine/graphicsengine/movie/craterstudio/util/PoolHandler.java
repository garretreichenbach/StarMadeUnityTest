/*
 * Created on Aug 7, 2005
 */
package org.schema.schine.graphicsengine.movie.craterstudio.util;

public interface PoolHandler<T>
{
   public T create();

   public void clean(T t);
}
