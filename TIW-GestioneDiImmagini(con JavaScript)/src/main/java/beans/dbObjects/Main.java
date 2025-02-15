package beans.dbObjects;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main {
	public static final int BLOCK = -2000;
	public static final int INFTY = 2000;
	public static int cont = 0;
	public static int[][] summerize (int[][] g, int n)
	{
		for (int i = 0 ; i < g.length ; i++)
		{
			for (int j = 0 ; j < g[0].length ; j++)
			{
				if (g[i][j] != BLOCK)
				{
					g[i][j] = g[i][j] + n;
				}
			}
		}
		return (g);
	}
	public static int[][] fillWithBlock (int[][] g)
	{
		for (int i = 0 ; i < g.length ; i++)
		{
			for (int j = 0 ; j < g[0].length ; j++)
			{
				g[i][j] = BLOCK;
			}
		}
		return (g);
	}
	public static void caller(int[][] graph)
	{
		long waitTime = 1; // 5 secondi

        // Crea un'istanza di ScheduledExecutorService
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Esegui il compito dopo il tempo specificato
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            throw new RuntimeException("Il tempo è scaduto!");
        }, waitTime, TimeUnit.MILLISECONDS);

        // Esegui il codice che vuoi "monitorare" nel timer
        try {
            // Simuliamo un'operazione che richiede tempo
            versioneM(graph);

            // Se l'operazione è completata prima della scadenza, cancella il timer
            future.cancel(false);
            System.out.println("Operazione completata prima della scadenza.");
        } catch (Exception e) {
            System.out.println("ERRORE");
        } finally {
        	cont++;
            // Arresta il scheduler
            scheduler.shutdown();
        }
	}
	public static void versioneM(int[][] graph)
	{
		ArrayList<Integer> q = new ArrayList<>();
		ArrayList<Integer> p = new ArrayList<>();
		ArrayList<Integer> d = new ArrayList<>();
		for (int i = 0 ; i < graph.length ; i++)
		{
			p.add(0);
			d.add(INFTY);
		}
		p.set(0,0);
		d.set(0,0);
		q.add(0);
		while (q.size()!=0)
		{
			int x = popTheBest(q,d);
			q = removefromlist(q, x);
			ArrayList<Integer> fs = new ArrayList<>();
			fs = fstar(graph, x);
			for (Integer i : fs)
			{
				if (d.get(x)+graph[x][i] < d.get(i))
				{
					p.set(i, x);
					d.set(i, d.get(x)+graph[x][i]);
					q.add(i);
				}
			}
		}
		System.out.println("esecuzione test "+cont);
		System.out.println(p);
	}
	public static ArrayList<Integer> removefromlist (ArrayList<Integer> q, Integer x)
	{
		ArrayList<Integer> ret = new ArrayList<>();
		for (Integer i : q)
		{
			if (i != x)
			{
				ret.add(i);
			}
		}
		return (ret);
	}
	public static Integer popTheBest (ArrayList<Integer> q, ArrayList<Integer> d)
	{
		int best = q.get(0);
		for (int i = 1 ; i < q.size() ; i++)
		{
			if (d.get(q.get(i))<d.get(best))
			{
				best = i;
			}
		}
		return (best);
	}
	public static ArrayList<Integer> fstar (int[][] graph, int node)
	{
		ArrayList<Integer> ret = new ArrayList<>();
		for (int j = 0 ; j<graph.length ; j++)
		{
			if (graph[node][j]!=BLOCK)
			{
				ret.add(j);
			}
		}
		return (ret);
	}
	public static void main (String[] args)
	{
		int[][] g1 = fillWithBlock(new int[4][4]);
		g1[0][1] = 2;
		g1[1][2] = -5;
		g1[2][3] = -11;
		g1[3][1] = -6;
		caller(g1);
		int[][] g2 = summerize(g1, 11);caller(g2);
		int[][] g3 = fillWithBlock(new int[5][5]);
		g3[0][1] = 8;
		g3[0][2] = 0;
		g3[2][3] = 4;
		g3[3][4] = 0;
		g3[1][2] = -16;
	    g3[3][4] = -8;
	    caller(g3);
		int[][] g4 = summerize(g3, 16);caller(g4);
		int[][] g5 = fillWithBlock(new int[6][6]);
		g5[0][1] = 2;
		g5[0][2] = 3;
		g5[2][1] = -2;
		g5[2][3] = 3;
		g5[3][2] = -6;
		g5[1][4] = 4;
		g5[4][3] = 4;
		g5[5][4] = 4;
		g5[3][4] = -5;
		caller(g5);
		int[][] g6 = summerize (g5, 6);caller(g6);
		
		
		
		
		
		
		
	}
}
