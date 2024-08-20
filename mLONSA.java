import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.LinkedList;

public class mLONSA {
    public static void main(String[] args) throws IOException {
        int n = 16; // Number of points
        int d = 4; // Dimension
        int K = 4; // Number of fronts
        long seed = 44; // Seed value
        Helper helper = new Helper();

        Point[] population = new Point[n];
        for(int i = 0; i < n; i++) {
            population[i] = new Point(d);
        }

        /* Random dataset */
        //population = genearteRandomData(n, d, seed);
        
        /* Worst-case dataset of LONSA */
        population = genearteWorstCaseData(n, K, d); 
        
        System.out.println("Population is...");
        for(int i = 0; i < n ; i++) {
            System.out.println(population[i]);
        }
        
        /* Call LONSA */
        helper.sortLONSA(population);
        
        /* Call mLONSA: Before this, set the label of the points in the population to 1 */
        for(int i = 0; i < n ; i++) {
            population[i].setLabel(1);
        }
        helper.sortmLONSA(population);
    }
    
    public static Point[] genearteRandomData(int n, int d, long seed) {
        Random random = new Random(seed);
        Point[] population = new Point[n];
        for (int i = 0; i < n; ++i) {
            population[i] = new Point(d);
            double[] objectives = new double[d];
            for (int j = 0; j < d; ++j) {
                objectives[j] = random.nextDouble();
            }
            population[i] = new Point(i, 1, objectives);
        }
        return population;
    }
    
    public static Point[] genearteWorstCaseData(int n, int K, int d) {
        Point[] population = new Point[n];
        for (int i = 0; i < n; ++i) {
            population[i] = new Point(d);
        }
        
        int Nk = n/K;
        int mminus2 = 1;
        int mminus1 = n;
        int id = 0;
        for(int k = 0; k < K; k++) {
            for(int i = 0; i < Nk-1; i++) {
                double[] objectives = new double[d];
                for(int j = 0; j < d-2; j++) {
                    objectives[j] = 1;
                }
                objectives[d-2] = mminus2;
                mminus2++;
                objectives[d-1] = mminus1;
                mminus1--;
                population[id] = new Point(id, 1, objectives);
                id++;
            }
            mminus2++;
        }   
        for(int k = K-1; k >=0; k--) {
            double[] objectives = new double[d];
            for(int j = 0; j < d-2; j++) {
                objectives[j] = 1;
            }
            objectives[d-2] = K*(k+1);
            objectives[d-1] = k+1;
            population[id] = new Point(id, 1, objectives);
            id++;
        }
        return population;
    }
}


class Point {
    private int id;
    private int label;
    private double[] objectives;
              
    public Point () {
        
    }
    
    public Point(int noObjectives) {
        this.objectives = new double[noObjectives];
    }
    
    public Point(Point p) {
        this.id = p.id;
        this.label = p.label;
        this.objectives = new double[p.objectives.length];
        for(int i = 0; i < p.objectives.length; i++) {
            this.objectives[i] = p.objectives[i];
        }
    }
        
    public Point(int id, int label, double[] objectives) {
        this.id = id;
        this.label = label;
        this.objectives = new double[objectives.length];
        for(int i = 0; i < objectives.length; i++) {
            this.objectives[i] = objectives[i];
        }   
    }

    public int getId() {
        return this.id;
    }

    public int getLabel() {
        return label;
    }

    public double[] getObjectives() {
        return this.objectives;
    }
    
    public double getObjective(int index) {
        return this.objectives[index];
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public void setObjectives(double[] objectives) {
        this.objectives = new double[objectives.length];
        for(int i = 0; i < objectives.length; i++) {
            this.objectives[i] = objectives[i];
        }
    }
    
    public void setObjective(double objective, int index) {
        this.objectives[index] = objective;
    }

    public int noObjectives(){
        return this.objectives.length;
    }

    @Override
    public String toString() {
        return "Point{" + "id=" + this.id + ", label=" + this.label + ", objectives=" + Arrays.toString(this.objectives) + '}';
    }
    
    /* Used for pre-sorting as in ENS
     * 1: First point is having small value for a objctive 
      -1: Second point is having small value for a objctive 
       0: Same */
    public int isSmall(Point p) {
        int noObjectives = p.getObjectives().length;
        for(int i = 0; i < noObjectives; i++) {
            if(this.objectives[i] < p.objectives[i]) {
                return 1;
            } else if (this.objectives[i] > p.objectives[i]) {
                return -1;
            }
        }
        return 0;
    }
    
    /* Used for pre-sorting as in BOS
     * 1: First solution is having small value for a objctive function 
      -1: Second solution is having small value for a objctive function */
    public int isSmall(Point sol, int[] Q0Order, int m) {
        if(this.objectives[m] < sol.objectives[m]) {
            return 1;
        } else if(this.objectives[m] > sol.objectives[m]) {
            return -1;
        } else {
            if(Q0Order[this.id] < Q0Order[sol.id]) {
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    /* 1: objectives dominates point 
      -1: point dominates objectives
       0: objectives and point is non-dominting */
    public int dominanceRelationship(Point sol) {
        boolean flag1 = false;
        boolean flag2 = false;
        int noObjectives = sol.objectives.length;
        for(int i = 0; i < noObjectives; i++) {
            if(this.objectives[i] < sol.objectives[i]) {
                flag1 = true;
            } else if(this.objectives[i] > sol.objectives[i]) {
                flag2 = true;
            }
        }
        if(flag1 == true && flag2 == false) {
            return 1;
        } else if(flag1 == false && flag2 == true) {
            return -1;
        } else {
            /*--  but in this case non-dominting --*/
            return 0;
        }
    } 
    
    /* 1: objectives dominates point 
      -1: point dominates objectives
       0: objectives and point is non-dominting */
    public int dominanceRelationship1(Point sol) {
        boolean flag1 = false;
        boolean flag2 = false;
        int noObjectives = sol.objectives.length;
        for(int i = 0; i < noObjectives; i++) {
            if(this.objectives[i] < sol.objectives[i]) {
                if(flag2) 
                    return 0;
                if(!flag1) 
                    flag1 = true;
            } else if(this.objectives[i] > sol.objectives[i]) {
                if(flag1) 
                    return 0;
                if(!flag2) 
                    flag2 = true;
            }
        }
        if(flag1 == true) {
            return 1;
        } else if(flag2 == true) {
            return -1;
        } else {
            /*--  but in this case non-dominting --*/
            return 0;
        }
    } 
}


class Helper {
    public void sortLONSA(Point population[]) {
        System.out.println("\n******************************* LONSA *******************************");
        int n = population.length;
        int count = 0;
        
        int[] array_x = new int[n];
        int[] array_y = new int[n];
        int[] index_array_x = new int[n];
        int[] index_array_y = new int[n];
        
        MergeSort ms = new MergeSort();
        array_x = ms.sortFirstObjective1(population);
        array_y = ms.sortSecondObjective1(population);
        
        for(int i = 0; i < n; i++) {
            index_array_x[array_x[i]] = i;
            index_array_y[array_y[i]] = i;
        }
        System.out.println("array_x: " + Arrays.toString(array_x));
        System.out.println("index_array_x: " + Arrays.toString(index_array_x));
        System.out.println("array_y: " + Arrays.toString(array_y));
        System.out.println("index_array_y: " + Arrays.toString(index_array_y));

        LinkedList<LinkedList<Integer>> frontSet = new LinkedList<>();
        
        int remainRankedPoints = n;
        int p, pos_x, pos_y;
        
        while(remainRankedPoints != 0) { 
            for(int i = 0; i < array_x.length; i++) {
                p = array_x[i];
                if(population[p].getLabel() == 1) {
                    population[p].setLabel(2); 
                    pos_y = index_array_y[p]+1;
                    while(pos_y < array_y.length) {
                        if(population[array_y[pos_y]].getLabel() != 3) {
                            count++;
                            int dom = population[p].dominanceRelationship(population[array_y[pos_y]]);
                            if(dom == 1) {
                                population[array_y[pos_y]].setLabel(3);
                                pos_x = index_array_x[array_y[pos_y]];
                                population[array_x[pos_x]].setLabel(3);
                            } else if(dom == -1) {
                                pos_y = index_array_y[p];
                                population[array_y[pos_y]].setLabel(3);
                                pos_x = index_array_x[p];
                                population[array_x[pos_x]].setLabel(3);
                                break;
                            }
                        }
                        pos_y = pos_y + 1;
                    }
                }
            }
            LinkedList<Integer> F = new LinkedList<>();
            for(int i = 0; i < array_x.length; i++) {
                p = array_x[i];
                if(population[p].getLabel() == 2) {
                    F.add(p);
                    remainRankedPoints--;
                } else{
                    population[p].setLabel(1);
                }
            }
            frontSet.add(F);
            int[] array_Ux = new int[remainRankedPoints];
            int[] array_Uy = new int[remainRankedPoints];
            int index_x = 0;
            int index_y = 0;
            for(int i = 0; i < array_x.length; i++) {
                int px = array_x[i];
                int py = array_y[i];
                if(population[px].getLabel() != 2) {
                    array_Ux[index_x] = px;
                    index_array_x[px] = index_x;
                    index_x++;
                }
                if(population[py].getLabel() != 2) {
                    array_Uy[index_y] = py;
                    index_array_y[py] = index_y;
                    index_y++;
                }
            }
            array_x = new int[remainRankedPoints];
            array_y = new int[remainRankedPoints];
            array_x = array_Ux;
            array_y = array_Uy;
        }
        System.out.println("FrontSet...");
        for(LinkedList<Integer> F: frontSet) {
            System.out.println("    F = " + F);
        }
        System.out.println("LONSA count: " + count);
    }

    public void sortmLONSA(Point population[]) {
        System.out.println("\n******************************* mLONSA *******************************");
        int n = population.length;
        int count = 0;
        
        int[] array_x = new int[n];
        int[] array_y = new int[n];
        int[] index_array_x = new int[n];
        int[] index_array_y = new int[n];

        MergeSort ms = new MergeSort();
        array_x = ms.sortFirstObjective(population);
        for(int i = 0; i < n; i++) {
            index_array_x[array_x[i]] = i;
        }
        array_y = ms.sortSecondObjective(population, index_array_x);
        for(int i = 0; i < n; i++) {
            index_array_y[array_y[i]] = i;
        }
        
        System.out.println("array_x: " + Arrays.toString(array_x));
        System.out.println("index_array_x: " + Arrays.toString(index_array_x));
        System.out.println("array_y: " + Arrays.toString(array_y));
        System.out.println("index_array_y: " + Arrays.toString(index_array_y));
        
        LinkedList<LinkedList<Integer>> frontSet = new LinkedList<>();
        int dComp = 0;
        
        int remainRankedPoints = n;
        int p, pos_x, pos_y;
        
        while(remainRankedPoints != 0) { 
            for(int i = 0; i < array_x.length; i++) {
                p = array_x[i];
                if(population[p].getLabel() == 1) {
                    population[p].setLabel(2); 
                    pos_y = index_array_y[p]+1;
                    while(pos_y < array_y.length) {
                        if(population[array_y[pos_y]].getLabel() != 3) {
                            count++;
                            int dom = population[p].dominanceRelationship(population[array_y[pos_y]]);
                            if(dom == 1) {
                                population[array_y[pos_y]].setLabel(3);
                            } 
                        }
                        pos_y = pos_y + 1;
                    }
                }
            }
            LinkedList<Integer> F = new LinkedList<>();
            for(int i = 0; i < array_x.length; i++) {
                p = array_x[i];
                if(population[p].getLabel() == 2) {
                    F.add(p);
                    remainRankedPoints--;
                } else{
                    population[p].setLabel(1);
                }
            }
            frontSet.add(F);
            
            int[] array_Ux = new int[remainRankedPoints];
            int[] array_Uy = new int[remainRankedPoints];
            int index_x = 0;
            int index_y = 0;
            for(int i = 0; i < array_x.length; i++) {
                int px = array_x[i];
                int py = array_y[i];
                if(population[px].getLabel() != 2) {
                    array_Ux[index_x] = px;
                    index_array_x[px] = index_x;
                    index_x++;
                }
                if(population[py].getLabel() != 2) {
                    array_Uy[index_y] = py;
                    index_array_y[py] = index_y;
                    index_y++;
                }
            }
            array_x = new int[remainRankedPoints];
            array_y = new int[remainRankedPoints];
            array_x = array_Ux;
            array_y = array_Uy;
        }
        System.out.println("FrontSet...");
        for(LinkedList<Integer> F: frontSet) {
            System.out.println("    F = " + F);
        }
        System.out.println("mLONSA count: " + count);
    }
}


class MergeSort {
    void mergeFirstObjective(Point population[], int arr[], int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;
        
        int L[] = new int[n1];
        int R[] = new int[n2];
 
        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];
 
        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if(population[L[i]].isSmall(population[R[j]]) != -1) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }
 
    void sortFirstObjective(Point population[], int arr[], int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            sortFirstObjective(population, arr, l, m);
            sortFirstObjective(population, arr, m + 1, r);
            mergeFirstObjective(population, arr, l, m, r);
        }
    }
    
    int[] sortFirstObjective(Point population[]) {
        int n = population.length;
        int[] Q0 = new int[n];
        for(int i = 0; i < n; i++) {
            Q0[i] = population[i].getId();
        }
        sortFirstObjective(population, Q0, 0, population.length-1);
        return Q0;
    }

    void mergeSecondObjective(Point population[], int arr[], int Q0Order[], int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;

        int L[] = new int[n1];
        int R[] = new int[n2];
 
        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];

        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if(population[L[i]].isSmall(population[R[j]],Q0Order, 1) != -1) {  // L[i] is either small or same
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            } 
            k++;
        }
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }
 
    void sortSecondObjective(Point population[], int arr[], int Q0Order[], int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            sortSecondObjective(population, arr, Q0Order, l, m);
            sortSecondObjective(population, arr, Q0Order, m + 1, r);
            mergeSecondObjective(population, arr, Q0Order, l, m, r);
        }
    }
    
    int[] sortSecondObjective(Point population[], int Q0Order[]) {
        int n = population.length;
        int[] Q0 = new int[n];
        for(int i = 0; i < n; i++) {
            Q0[i] = population[i].getId();
        }
        sortSecondObjective(population, Q0, Q0Order, 0, population.length-1);
        return Q0;
    }
    
    void mergeFirstObjective1(Point population[], int arr[], int l, int m, int r) {
        int n1 = m - l + 1; 
        int n2 = r - m;

        int L[] = new int[n1]; 
        int R[] = new int[n2];

        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];
 
        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if(population[L[i]].getObjective(0) <= population[R[j]].getObjective(0) ) { 
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }
 
    void sortFirstObjective1(Point population[], int arr[], int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            sortFirstObjective1(population, arr, l, m);
            sortFirstObjective1(population, arr, m + 1, r);
            mergeFirstObjective1(population, arr, l, m, r);
        }
    }
    
    int[] sortFirstObjective1(Point population[]) {
        int n = population.length;
        int[] Q0 = new int[n];
        for(int i = 0; i < n; i++) {
            Q0[i] = population[i].getId();
        }
        sortFirstObjective1(population, Q0, 0, population.length-1);
        return Q0;
    }
    
    void mergeSecondObjective1(Point population[], int arr[], int l, int m, int r) {
        int n1 = m - l + 1; 
        int n2 = r - m;
        
        int L[] = new int[n1]; 
        int R[] = new int[n2];

        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];
 
        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if(population[L[i]].getObjective(1) <= population[R[j]].getObjective(1) ) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            } 
            k++;
        }
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }
 
    void sortSecondObjective1(Point population[], int arr[], int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            sortSecondObjective1(population, arr, l, m);
            sortSecondObjective1(population, arr, m + 1, r);
            mergeSecondObjective1(population, arr, l, m, r);
        }
    }
    
    int[] sortSecondObjective1(Point population[]) {
        int n = population.length;
        int[] Q0 = new int[n];
        for(int i = 0; i < n; i++) {
            Q0[i] = population[i].getId();
        }
        sortSecondObjective1(population, Q0, 0, population.length-1);
        return Q0;
    }
}
