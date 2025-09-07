package org.schema.schine.graphicsengine.forms.quadtree;

public class QuadTree {
	// quadtree.cpp	-thatcher 9/15/1999 Copyright 1999-2000 Thatcher Ulrich

	// Code for quadtree terrain manipulation, meshing, and display.

	// This code may be freely modified and redistributed.  I make no
	// warrantees about it; use at your own risk.  If you do incorporate
	// this code into a project, I'd appreciate a mention in the credits.
	//
	// Thatcher Ulrich <tu@tulrich.com>

	//	public class ClipBox{
	//		int right, left, bottom, top;
	//	}
	//	void	DrawQuadTree( QuadSquare quad, int xorigin, int yorigin, int BlockPixels, final quadcornerdata pd, boolean NoClip)
	//	// Draws the given quadtree height data as a contour map, with the
	//	// quadtree's surface data as blocks of color in the background.
	//	{
	//		int	i, j;
	//
	//		if (!NoClip) {
	//			ClipBox	r = null;
	////			dc->GetClipBox(&r);	//r.left,r.right,r.top,r.bottom;
	//
	//
	//			if (xorigin >= r.right || xorigin + BlockPixels < r.left || yorigin >= r.bottom || yorigin + BlockPixels < r.top) {
	//				// This square does not touch the rendering area; no need to draw.
	//				return;
	//			} else if (xorigin >= r.left && xorigin + BlockPixels < r.right && yorigin >= r.top && yorigin + BlockPixels < r.bottom) {
	//				// Square is completely inside rendering
	//				// rectangle; so any child blocks will also be
	//				// within the rendering area and we don'transformationArray need
	//				// to check them.
	//				NoClip = true;
	//			}
	//		}
	//
	//		for (j = 0; j < 2; j++) {
	//			for (i = 0; i < 2; i++) {
	//				int	ChildIndex = 2 * j + (j != 0 ? i : 1 - i);
	//
	//				quadcornerdata	q = null;
	//				quad.SetupCornerData(q, pd, ChildIndex);
	//
	//				int	half = BlockPixels >> 1;
	//				int	x = xorigin + half * i;
	//				int	y = yorigin + half * j;
	//
	//				if (BlockPixels > 8 /* lower limit for block size */ && quad.Child[ChildIndex] != null) {
	//					// Recurse to child block.
	//					DrawQuadTree(quad.Child[ChildIndex], x, y, half, q, NoClip);
	//				} else {
	//					// Draw this square directly.
	////					draw surface type background;
	//
	//					DrawContourBox( x, y, half, q.Verts);
	//				}
	//			}
	//		}
	//	}
	//
	//
	//
	//
	//
	//	void	DrawHeightField(int xorigin, int yorigin, int Size, QuadSquare heightfield, float ContourInterval)
	//	// Renders a height field into the given device context.
	//	{
	//		int	i;
	//// ifdef NOT
	//		// Draw surface-type background.
	//		int	x, y;
	//		y = yorigin;
	//		float	RowStart = DataGrid;
	//		 char	TypeRowStart = TypeDataGrid;
	//		for (int j = 0; j < zsize; j++) {
	//			float	multiTexturePathPattern = RowStart;
	//			 char	transformationArray = TypeRowStart;
	//			x = xorigin;
	//			for (int i = 0; i < xsize; i++) {
	//				// Show surface type behind the contour lines.
	//				pDC.FillSolidRect(x, y, BlockPixels, BlockPixels, TypeColor(transformationArray));
	//
	//				multiTexturePathPattern++;
	//				transformationArray++;
	//				x += BlockPixels;
	//			}
	//			RowStart += GridPitch;
	//			TypeRowStart += GridPitch;
	//			y += BlockPixels;
	//		}
	//// endif NOT
	//
	//		//
	//		// Draw contour lines.
	//		//
	//
	//		cp.OneOverInterval = 1.0 / ContourInterval;
	//
	//		// Setup pens.
	//		if (cp.PensCreated == false) {
	//			for (i = 0; i < PENCOUNT; i++) {
	//				cp.pen[i].CreatePen(PS_SOLID, 1, RGB(0 + 40 * i, 0 + 40 * i, 20 + 40 * i));
	//			}
	//		}
	//
	//		// Select a new pen into the DC.
	////		CPen*	OldPen = dc->SelectObject(&cp.pen[0]);
	//
	//		// Draw quadtree.
	//		quadcornerdata	q;
	//		q.Level = 15;
	//		q.ChildIndex = 0;
	//		q.xorg = q.zorg = 0;
	//		q.Verts[1].Y = 0;
	//		q.Verts[2].Y = 0;
	//		q.Verts[3].Y = 0;
	//		q.Verts[4].Y = 0;
	//
	//		DrawQuadTree(dc, heightfield, xorigin, yorigin, Size >> 1, q, false);
	//
	//		// Restore the original pen.
	////		dc->SelectObject(OldPen);
	//
	//	}
	//
	//
	////	void	DrawContourBox(CDC* dc, int x, int y, int BlockPixels, VertInfo samples[4])
	////	// Draws the contour lines on a single square sub-element of a contour
	////	// map, given the heights of the four corners and some setup parameters
	////	// in the ContourParams cp structure.
	////	{
	////		// Draw contour lines.
	////		float	samp[4];
	////		samp[0] = samples[0].Y * cp.OneOverInterval;	// upper left
	////		samp[1] = samples[1].Y * cp.OneOverInterval;	// upper right
	////		samp[2] = samples[2].Y * cp.OneOverInterval;	// lower right
	////		samp[3] = samples[3].Y * cp.OneOverInterval;	// lower left
	////
	////		int	min = 100000;
	////		int	max = -100000;
	////		int	isamp[4];
	////		for (int k = 0; k < 4; k++) {
	////			// Keep samples within reasonable bounds.
	////			if (samp[k] < -300) samp[k] = -300;
	////			if (samp[k] > 10000) samp[k] = 10000;
	////
	////			// Convert to integer.
	////			isamp[k] = floor(samp[k]);
	////
	////			// Compute min & max.
	////			if (isamp[k] < min) min = isamp[k];
	////			if (isamp[k] > max) max = isamp[k];
	////		}
	////
	////		// For each interval crossing within the range of values, draw the
	////		// appropriate line across the box.
	////		for (k = min + 1; k <= max; k++) {
	////			int	EdgePix[4];
	////
	////			// Choose the appropriate pen.
	////			dc->SelectObject(&cp.pen[k % PENCOUNT]);
	////
	////			// Check each edge to see if this contour
	////			// crosses it.
	////			for (int l = 0; l < 4; l++) {
	////				if ((isamp[(l+3)&3] < k && isamp[l] >= k) ||
	////				    (isamp[(l+3)&3] >= k && isamp[l] < k))
	////				{
	////					float	num = k - samp[(l+3)&3];
	////					if (l == 0 || l == 3) {
	////						EdgePix[l] = BlockPixels * (1 - num / (samp[l] - samp[(l+3)&3]));
	////					} else {
	////						EdgePix[l] = BlockPixels * num / (samp[l] - samp[(l+3)&3]);
	////					}
	////				} else {
	////					EdgePix[l] = -1;
	////				}
	////			}
	////
	////			// Connect the edges.
	////			for (l = 0; l < 4; l++) {
	////				if (EdgePix[l] == -1) continue;
	////				for (int m = l+1; m < 4; m++) {
	////					if (EdgePix[m] != -1) {
	////						// Connect edge l with m.
	////						int	xs, ys;
	////						if (l & 1) {
	////							xs = x + EdgePix[l];
	////							ys = y + (l == 3 ? BlockPixels : 0);
	////						} else {
	////							xs = x + (l == 2 ? BlockPixels : 0);
	////							ys = y + EdgePix[l];
	////						}
	////
	////						int	xe, ye;
	////						if (m & 1) {
	////							xe = x + EdgePix[m];
	////							ye = y + (m == 3 ? BlockPixels : 0);
	////						} else {
	////							xe = x + (m == 2 ? BlockPixels : 0);
	////							ye = y + EdgePix[m];
	////						}
	////
	////						if (l == 2 && m == 3) {
	////							dc->MoveTo(xe, ye);
	////							dc->LineTo(xs, ys);
	////						} else {
	////							dc->MoveTo(xs, ys);
	////							dc->LineTo(xe, ye);
	////						}
	////
	////						if (m > l) l = m;
	////						break;
	////					}
	////				}
	////			}
	////		}
	////	}
	////
	////
	////	#endif // NOT
	////
	////
	////	unsigned int	AvgColor(unsigned int a, unsigned int b)
	////	// Returns the average of the two colors.  Interprets colors as 32-bit ARGB,
	////	// each component being 8 bits.
	////	{
	////		int	c = 0xFF000000;	// We're just going to hard-code A to FF for now.
	////
	////		a >>= 1;
	////		b >>= 1;
	////
	////		c |= (a & 0x07F7F7F) + (b & 0x07F7F7F);
	////
	////		return c;
	////	}
	//
	//
	//
	//	//
	//	// quadsquare functions.
	//	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	////
	////	void	Update(const quadcornerdata& cd, const float ViewerLocation[3], float Detail)
	////	// Refresh the vertex enabled states in the tree, according to the
	////	// location of the viewer.  May force creation or deletion of qsquares
	////	// in areas which need to be interpolated.
	////	{
	////		DetailThreshold = Detail * VERTICAL_SCALE;
	////
	////		UpdateAux(cd, ViewerLocation, 0);
	////	}
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//

}
