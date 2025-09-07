/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>PDFLoader</H2>
 * <H3>org.schema.schine.graphicsengine.pdf</H3>
 * PDFLoader.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright © 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.graphicsengine.pdf;

//import com.sun.pdfview.PDFFile;
//import com.sun.pdfview.PDFPage;


import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * The Class PDFLoader.
 */
public class PDFLoader implements ImageObserver {

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
				return false;
	}

	/**
//	 * The current xform.
//	 */
//	AffineTransform currentXform;
//	/**
//	 * The block.
//	 */
//	boolean block = false;
//	/**
//	 * The cur file.
//	 */
//	private PDFFile curFile;
//	/**
//	 * The doc description.
//	 */
//	private String docName;
//	/**
//	 * The curpage.
//	 */
//	private int curpage;
//	/**
//	 * The current page.
//	 */
//	private PDFPage currentPage;
//	/**
//	 * The prev size.
//	 */
//	private Dimension prevSize;
//	/**
//	 * The prev clip.
//	 */
//	private Rectangle2D prevClip;
//	/**
//	 * The current image.
//	 */
//	private Image currentImage;
//	/**
//	 * The clip.
//	 */
//	private Rectangle2D clip;
//	/**
//	 * The pages.
//	 */
//	private Texture[] pages;
//
//	/**
//	 * Changes the displayed page.
//	 *
//	 * @param pagenum the page to display
//	 */
//	public void forceGotoPage(int pagenum) {
//		if (pagenum <= 0) {
//			pagenum = 0;
//		} else if (pagenum >= curFile.getNumPages()) {
//			pagenum = curFile.getNumPages() - 1;
//		}
//		//        System.out.println("Going to page " + pagenum);
//		curpage = pagenum;
//
//		// update the page text field
//		//        pageField.setText(String.valueOf(curpage + 1));
//
//		// fetch the page and show it in the appropriate place
//		PDFPage pg = curFile.getPage(pagenum + 1);
//		//        if (fspp != null) {
//		//            fspp.showPage(pg);
//		//            fspp.requestFocus();
//		//        } else {
//		//            page.showPage(pg);
//		//            page.requestFocus();
//		//        }
//		showPage(pg);
//
//		// update the thumb panel
//		//        if (doThumb) {
//		//            thumbs.pageShown(pagenum);
//		//        }
//
//		// stop any previous page prepper, and start a new one
//		//        if (pagePrep != null) {
//		//            pagePrep.quit();
//		//        }
//		//        pagePrep = new PagePreparer(pagenum);
//		//        pagePrep.start();
//		//
//		//        setEnabling();
//	}
//
//	/**
//	 * @return the curpage
//	 */
//	public int getCurpage() {
//		return curpage;
//	}
//
//	/**
//	 * Gets the max pages.
//	 *
//	 * @return the max pages
//	 */
//	public int getMaxPages() {
//		return pages.length;
//	}
//
//	/**
//	 * Gets the page.
//	 *
//	 * @param site the site
//	 * @return the page
//	 */
//	public Texture getPage(int site) {
//		site = site < 0 ? 0 : (site >= pages.length ? pages.length - 1 : site);
//		if (pages[site] == null) {
//			throw new NullPointerException("pdf page not loaded: requested " + site + " of " + pages.length);
//		}
//		return pages[site];
//	}
//
//	/**
//	 * Gets the texture.
//	 *
//	 * @param b   the b
//	 * @param gl  the gl
//	 * @param glu the glu
//	 * @return the texture
//	 */
//	private Texture getTexture(Image b) {
//		throw new UnsupportedOperationException();
//		//		return new Texture(b,);
//	}
//
//	/* (non-Javadoc)
//	 * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
//	 */
//	@Override
//	public boolean imageUpdate(Image img, int infoflags, int x, int y,
//	                           int width, int height) {
//		//		try {
//		//			Thread.sleep(2000);
//		//		} catch (InterruptedException e) {
//		//			e.printStackTrace();
//		//		}
//		block = false;
//		return false;
//	}
//
//	/**
//	 * Load textures.
//	 *
//	 * @param gl  the gl
//	 * @param glu the glu
//	 */
//	public void loadTextures() {
//		pages = new Texture[curFile.getNumPages()];
//		for (int i = 0; i < pages.length; i++) {
//			block = true;
//			forceGotoPage(i);
//			while (block) {
//			}
//			;
//			try {
//				ImageIO.write((RenderedImage) currentImage, "jpg", new FileExt(DataUtil.dataPath + "pdf/" + i + ".jpg"));
//			} catch (IOException e) {
//
//				e.printStackTrace();
//			}
//			pages[i] = getTexture(currentImage);
//			System.err.println("[PDF] loaded page " + i + ": [" + pages[i].getWidth() + "x" + pages[i].getHeight() + "] of " + docName);
//		}
//	}
//
//	/**
//	 * Open a specific pdf file.  Creates a DocumentInfo from the file,
//	 * and opens that.
//	 *
//	 * @param file the file to open
//	 * @throws IOException Signals that an I/O exception has occurred.
//	 */
//	public void openFile(File file) throws IOException {
//		// first open the file for random access
//		RandomAccessFile raf = new RandomAccessFile(file, "r");
//
//		// extract a file channel
//		FileChannel channel = raf.getChannel();
//
//		// now memory-map a byte-bufferList
//		ByteBuffer buf =
//				channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
//
//		// create a PDFFile from the data
//		PDFFile newfile = null;
//		try {
//			newfile = new PDFFile(buf);
//		} catch (IOException ioe) {
//			throw new FileNotFoundException(file.getPath() + " doesn'transformationArray appear to be a PDF file.");
//			//            return;
//		}
//
//		// Now that we're reasonably sure this document is real, close the
//		// old one.
//		//        doClose();
//
//		// set up our document
//		this.curFile = newfile;
//		docName = file.getName();
//	}
//
//	/**
//	 * Show page.
//	 *
//	 * @param page the page
//	 */
//	public synchronized void showPage(PDFPage page) {
//		// stop drawing the previous page
//		if (currentPage != null && prevSize != null) {
//			currentPage.stop(prevSize.width, prevSize.height, prevClip);
//		}
//
//		// set up the new page
//		currentPage = page;
//
//		if (page == null) {
//			// no page
//			currentImage = null;
//			clip = null;
//			currentXform = null;
//			//                repaint();
//		} else {
//			// start drawing -- clear the flag to indicate we're in progress.
//			//    	    flag.clear();
//			//	    System.out.println("   flag cleared");
//
//			Dimension sz = new Dimension(300, 300);
//			if (sz.width + sz.height == 0) {
//				// no image to draw.
//				return;
//			}
//			//	    System.out.println("Ratios: scrn="+((float)sz.width/sz.height)+
//			//			       ", clip="+(clip==null ? 0 : clip.getWidth()/clip.getHeight()));
//
//			// calculate the clipping rectangle in page space from the
//			// desired clip in screen space.
//			Rectangle2D useClip = clip;
//			if (clip != null && currentXform != null) {
//				useClip = currentXform.createTransformedShape(clip).getBounds2D();
//			}
//
//			Dimension pageSize = page.getUnstretchedSize(sz.width, sz.height,
//					useClip);
//
//			// get the new image
//			currentImage = page.getImage(pageSize.width * 2, pageSize.height * 2,
//					useClip, this);
//
//			// calculate the transform from screen to page space
//			currentXform = page.getInitialTransform(pageSize.width,
//					pageSize.height,
//					useClip);
//			try {
//				currentXform = currentXform.createInverse();
//			} catch (NoninvertibleTransformException nte) {
//				System.out.println("Error inverting page transform!");
//				nte.printStackTrace();
//			}
//
//			prevClip = useClip;
//			prevSize = pageSize;
//
//			//                repaint();
//		}
//	}
}
