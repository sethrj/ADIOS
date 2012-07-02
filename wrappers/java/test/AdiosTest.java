import gov.ornl.ccs.Adios;
import gov.ornl.ccs.AdiosFile;
import gov.ornl.ccs.AdiosGroup;
import gov.ornl.ccs.AdiosVarinfo;
import java.nio.ByteBuffer;

public class AdiosTest
{
    // The main program
    public static void main(String[] args)
    {
        System.out.println(">>> AdiosJava Test Drive");
        System.out.println(">>> ADIOS Write API ... ");
        
        Adios.MPI_Init(new String[0]);
        long comm = Adios.MPI_COMM_WORLD();
        int rank = Adios.MPI_Comm_rank(comm);
        int size = Adios.MPI_Comm_size(comm);
        System.out.println("[DEBUG] MPI rank/size = " + rank + " / " + size);
        System.out.println("[DEBUG] MPI &comm     = " + comm);

        Adios.Init("config.xml");
        long adios_handle = Adios.Open ("temperature", "adios_global.bp", "w", comm);
        
        int      NX = 10; 
        double[] t = new double[NX];
        for (int i = 0; i < NX; i++) {
            t[i] = (double) i;
        }

        long groupsize = 4 + 4 + 4 + 8 * (1) * (NX);

        
        /*
        // Don't know why. It doesn't work in this way.
        long adios_handle = Adios.Open ("temperature", "adios_global.bp", "w", comm);
        long adios_totalsize = Adios.SetGroupSize(adios_handle, groupsize);
        */
        adios_handle = Adios.OpenAndSetGroupSize("temperature", "adios_global.bp", "w", groupsize, comm);

        Adios.Write (adios_handle, "NX", NX);
        Adios.Write (adios_handle, "size", size);
        Adios.Write (adios_handle, "rank", rank);
        Adios.Write (adios_handle, "temperature", t);

        Adios.Close (adios_handle);
        Adios.Finalize (rank);
        
        System.out.println(">>> ADIOS Read API ... ");

        AdiosFile file = new AdiosFile();
        file.open("adios_global.bp", comm);
        
        AdiosGroup group = new AdiosGroup(file);
        group.open("temperature");

        AdiosVarinfo var = new AdiosVarinfo(group);
        var.inq("temperature");

        long[] start = {0, 0};
        long[] count = {1, 10};
        double[] output = var.read(start, count);

        System.out.println("temperature.length = " + output.length);
        for (int i = 0; i < output.length; i++) {
            System.out.println("temperature[" + i + "] = " + output[i]);
        }

        AdiosVarinfo var2 = new AdiosVarinfo(group);
        var2.inq("NX");
        System.out.println("NX = " + var2.read());

        var.close();
        var2.close();
        group.close();
        file.close();

        System.out.println();
        System.out.println(file);
        System.out.println(group);
        System.out.println(var);
        System.out.println(var2);
    }
}