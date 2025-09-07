/*
 * Created on 15 jun 2010
 */

package org.schema.schine.graphicsengine.movie.craterstudio.func;

public interface Transformer<I, O>
{
   public O transform(I value);
}