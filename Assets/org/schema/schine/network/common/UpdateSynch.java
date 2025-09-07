package org.schema.schine.network.common;

import java.io.IOException;


public class UpdateSynch {

	private boolean changed = true;
	public UpdateSynch() {
	}
	
	public void updateLock(Updatable u, long timeBecomesConstantUpdating) throws IOException {
		synchronized(this) {
			while(!changed) {
				try {
					
					if(timeBecomesConstantUpdating == 0) {
						this.wait();
					}else {
						long delay = timeBecomesConstantUpdating-System.currentTimeMillis();
						if(delay > 0) {
							this.wait(delay);
						}else {
							//constant updating mode now
							//small delay for perfomance
							Thread.sleep(10);
						}
						changed = true; // do one update
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			changed = false;
		}
		
		u.update();
	}
	
	public void notfifyUpdateNeeded() {
		synchronized(this) {
			changed = true;
			this.notifyAll();
		}
	}

	public void updateLock(Updatable u) throws IOException {
		updateLock(u, 0);
	}

}
