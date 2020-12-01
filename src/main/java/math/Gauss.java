package main.java.math;

import java.util.ArrayList;


enum SingularityFlag {
    noSolution,
    cleanSolution,
    infiniteSolutions
};

/**
 * Contains methods for Gaussian elimination.
 * HUN: Gauss-eliminációt megvalósító metódusokat tartalmaz.
 * @author Simon Zoltán
 *
 */
public class Gauss {

    /**
     * Gaussian elimination for column major matrix.
     * HUN: Gauss-elimináció oszlop-folytonos mátrixra.
     * @param system M - (N+1)*N matrix The right side values should be in the last row. Will be modified!!!
     * @return Vector - The solution of the linear system. The 0. index value of the vector is the value of the 0. index variable.
     * @throws GaussException - if the matrix has no solution or has infinite solutions.
     */
    public static Vector Eliminate (Matrix system) throws GaussException {
    	Matrix M = new Matrix(0,0);		//Create a clone of the original to prevent from modifications.
    	M.copyWithResize(system);
    	
        SingularityFlag flag = Reduce(M);
        switch (flag) {
            case noSolution: {
                throw new NoSolution();
            }
            case infiniteSolutions: {
                throw new InfiniteSolutions();
            }
            case cleanSolution: {
    //Second fase of Gauss eliminaton after reducton:----------------------------
                int r = M.row-2;
                int rightSideRow = M.row-1;     //The right side of the equations.
                for (int c = M.column-1; c > 0; c--) {    //Reverse iteration on columns
                    for (int ci = c-1; ci >= 0; ci--) {
                            M.setAt(rightSideRow, ci, M.at(rightSideRow, ci) - M.at(rightSideRow, c) * M.at(r, ci));
                            M.setAt(r, ci, 0);
                    }
                    r--;
                }
                Vector ret = new Vector(M.row-1);
                for (int c = 0; c < M.column; c++) {
                    ret.setAt(c, M.at(rightSideRow, c));
                }
                return ret;
    //---------------------------------------------------------------------------
            }
            default:
                return new Vector(0);            	
        }
    }

    /**
	 * Reduces matrix to r.e.f.
	 * HUN: A mátrixot lineárisan függetlené redukálja.
	 * @param M - Matrix to reduce. Will be modified!
	 * @return SingularityFlag: {<br>
	 *                          &nbsp;&nbsp;noSolution,<br>
	 *                          &nbsp;&nbsp;cleanSolution,<br>
	 *                          &nbsp;&nbsp;infiniteSolutions<br>
	 *                          }<br>
	 */
    public static SingularityFlag Reduce(Matrix M) {
    	
    	if (M.column < M.row - 1) {
            return SingularityFlag.infiniteSolutions;
        }
    	

        int c = 0;
        int r = 0;
        
        while (true) {
            if (0 != M.at(r,c)) {
                //Divide column[c] by M(r,c).
                float divider = 1 / M.at(r, c);
                for (int i = 0; i < M.row; i++) {
                    M.setAt(i, c, M.at(i, c)* divider);
                }
                if (c < M.column - 1) {
                    //Add the -M(r, i) * M[c] column to all "M[i]" columns:
                    for (int i = c+1; i < M.column; i++) {
                        float fact = M.at(r, i);
                        for (int j = 0; j < M.row; j++) {
                            M.setAt(j, i, M.at(j, i) - fact * M.at(j, c));
                        }
                    }
                }
                if ((r == M.row - 2) || (c == M.column - 1)) {  //Crossed matrix
                    break;
                }
                else {                 //Proceed diagonally
                    r++;
                    c++;
                }
            }
            else {                    //if 0 == M(r,c)
                boolean foundSwappable = false;
                if (c < M.column-1) {
                    //Search for swapable. Criteria: 0 != M(r, i)
                    for (int i = c+1; i < M.column; i++) {
                        if (0 != M.at(r, i)) {     //if swappable found swap columns
                            SwapColumn(M, c, i);
                            foundSwappable = true;
                            break;
                        }
                    }
                }
                if (!foundSwappable) {  //No swappable found
                    if (r == M.row - 2) {   //if current row == n.
                        c--;
                        break;
                    }
                    else {
                        r++;
                    }
                }
            }
        }


        if (c == -1) {
        	return SingularityFlag.infiniteSolutions;
        }

        //Final steps of Reduce:
        if (c < M.column-1) {
            ArrayList<Integer> toRemoveIndexes = new ArrayList<>();     //Only if shrink matrix section is not commented out.
            for (int i = c; i < M.column; i++) {    //Iterate on columns
                boolean foundNotZeroInColumn = false;
                for (int j = 0; j < M.row; j++) {   //Iterate element in current column
                    if (0 != M.at(j, i)) {
                        if (j == M.row-1) {     //Found a forbidden column, where all elements are 0 except the last element.
                            return SingularityFlag.noSolution;
                        }
                        foundNotZeroInColumn = true;
                        break;
                    }
                }
                if (!foundNotZeroInColumn) {    //Zero columns should be removed.
                    toRemoveIndexes.add(i);
                }
            }

            //Shrink matrix by removing zero columns:-----------------------
            M.copyWithResize(MyMath.removeColumns(M, toRemoveIndexes));

            if (M.column < M.row - 1) {
                return SingularityFlag.infiniteSolutions;
            }

            return SingularityFlag.cleanSolution;
        }
        else {
            return SingularityFlag.cleanSolution;
            //return SingularityFlag::noSolution;
        }

    }


    //Auxiliary functions:

	/**
     * Swaps content of two columns of M matrix.
     * HUN: Megcserél két oszlopot a mátrixban.
     * @param M - Column major matrix
     * @param col1 - Column no. 1
     * @param col2 - Column no. 2
     */
    private static void SwapColumn (Matrix M, int col1, int col2) {
    	float temp;
        for (int r = 0; r < M.row; r++) {
            temp = M.at(r, col1);
            M.setAt(r, col1, M.at(r, col2));
            M.setAt(r, col2, temp);
        }

    }

    /**
     * @deprecated
     * Builds a coefficientMatrix;
     * Előállít egy együtthatós rendszert.
     * @param M - Left side of equations.
     * @param v - Right side of equations.
     * @return Matrix
     */
    public static Matrix CoefficientMatrix (Matrix M, Vector v) {
        if (M.column == v.dimension) {
            Matrix retM = new Matrix(M.row+1, M.column);
            for (int c = 0; c < M.column; c++) {
                for (int r = 0; r < M.row; r++) {
                    retM.setAt(r, c, M.at(r, c));
                }
                retM.setAt(M.row, c, v.at(c));
            }
            return retM;
        }
        else {
            throw new RuntimeException("Not equal dimensions, when creating coefficient matrix.");
        }
    }
    
}

