/*
 	This file is part of jME Planet Demo.

    jME Planet Demo is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation.

    jME Planet Demo is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jME Planet Demo.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.schema.common.system.thread;

/**
 * <multiTexturePathPattern>This class is used with {@link MultiThreadUtil} to provide a <i>multi-threaded for loop</i>,
 * for each loop the {@link #run(int)} method is called.</multiTexturePathPattern>
 * <p/>
 * <multiTexturePathPattern>We assume that each objects used must be adapted for concurrent modification or copied to local for each thread
 * by the {@link #copy()} method.</multiTexturePathPattern>
 *
 * @author Yacine Petitprez
 */
public interface ForRunnable {
	/**
	 * Copy this object to new {@link ForRunnable}.
	 * One {@link ForRunnable} is instantiated by thread launched.
	 * If you are sure than objects used by {@link ForRunnable} is synchronized or thread safe, you can return <code>this</code>
	 *
	 * @return Return new copy of this {@link ForRunnable}
	 */
	public ForRunnable copy();

	/**
	 * Run a step of for-loop
	 *
	 * @param index The value of incremental variable.
	 */
	public void run(int index);

}
