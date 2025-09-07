package api.utils.other;

import it.unimi.dsi.fastutil.Stack;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class ObjectArrayStack<E> implements Stack<E> {

	private final int limit;
	private Object[] stack;
	private final boolean autoTrim;

	public ObjectArrayStack(int limit, boolean autoTrim) {
		if(limit == -1) limit = 10;
		this.limit = limit;
		this.autoTrim = autoTrim;
		stack = new Object[limit];
	}
	
	public ObjectArrayStack(boolean autoTrim) {
		this(-1, true);
	}
	
	public ObjectArrayStack(int limit) {
		this(limit, true);
	}

	public ObjectArrayStack() {
		this(-1, true);
	}

	@Override
	public void push(E o) {
		if(autoTrim) trim();
		if(stack.length >= limit) {
			Object[] newStack = new Object[stack.length + 1];
			System.arraycopy(stack, 0, newStack, 0, stack.length);
			stack = newStack;
		}
	}

	@Override
	public E pop() {
		if(autoTrim) trim();
		E o = (E) stack[stack.length - 1];
		stack[stack.length - 1] = null;
		if(autoTrim) trim();
		return o;
	}

	@Override
	public boolean isEmpty() {
		if(autoTrim) trim();
		return stack.length == 0;
	}

	@Override
	public E top() {
		if(autoTrim) trim();
		return (E) stack[stack.length - 1];
	}

	@Override
	public E peek(int i) {
		if(autoTrim) trim();
		return (E) stack[i];
	}
	
	public void trim() {
		int i = 0;
		for(Object o : stack) {
			if(o == null) stack[i] = null;
			i++;
		}
		Object[] newStack = new Object[i];
		System.arraycopy(stack, 0, newStack, 0, i);
		stack = newStack;
	}
	
	public boolean contains(Object o) {
		for(Object obj : stack) {
			if(obj.equals(o)) return true;
		}
		return false;
	}
	
	public void clear() {
		stack = new Object[limit];
	}
	
	public int size() {
		if(autoTrim) trim();
		return stack.length;
	}
	
	public E[] toArray() {
		return (E[]) stack;
	}

	public void remove(E index) {
		if(autoTrim) trim();
		for(int i = 0; i < stack.length; i++) {
			if(stack[i].equals(index)) {
				stack[i] = null;
				break;
			}
		}
		if(autoTrim) trim();
	}
}
