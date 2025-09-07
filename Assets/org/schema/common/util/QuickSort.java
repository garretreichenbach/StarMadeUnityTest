package org.schema.common.util;

public class QuickSort {

	public static void sort(int start, int end, ArraySortInterface<Float> array) {
		int i = start; // index of left-to-right scan
		int k = end; // index of right-to-left scan

		if (end - start >= 1) // check that there are at least two elements to
		// sort
		{
			float pivot = array.getValue(start); // set the pivot as the first
			// element in the partition

			while (k > i) // while the scan indices from left and right have not
			// met,
			{
				while (array.getValue(i) <= pivot && i <= end && k > i)
					// from the left, look for the first
					i++; // element greater than the pivot
				while (array.getValue(k) > pivot && k >= start && k >= i)
					// from the right, look for the first
					k--; // element not greater than the pivot
				if (k > i) // if the left seekindex is still smaller than
					array.swapValues(i, k); // the right index, swap the
				// corresponding elements
			}
			array.swapValues(start, k); // after the indices have crossed, swap
			// the last element in
			// the left partition with the pivot
			sort(start, k - 1, array); // quicksort the left partition
			sort(k + 1, end, array); // quicksort the right partition
		} else // if there is only one element in the partition, do not do any
		// sorting
		{
			return; // the array is sorted, so exit
		}
	}

	public static void sort(int array[], int low, int n) {
		int lo = low;
		int hi = n;
		if (lo >= n) {
			return;
		}
		int mid = array[(lo + hi) / 2];
		while (lo < hi) {
			while (lo < hi && array[lo] < mid) {
				lo++;
			}
			while (lo < hi && array[hi] > mid) {
				hi--;
			}
			if (lo < hi) {
				int T = array[lo];
				array[lo] = array[hi];
				array[hi] = T;
			}
		}
		if (hi < lo) {
			int T = hi;
			hi = lo;
			lo = T;
		}
		QuickSort.sort(array, low, lo);
		QuickSort.sort(array, lo == low ? lo + 1 : lo, n);
	}

}
