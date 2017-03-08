package visualizer.openCL;

import com.nativelibs4java.opencl.*;
import com.nativelibs4java.util.IOUtils;
import org.bridj.Pointer;
import visualizer.engine.Vector3d;
import visualizer.engine.transform.TreeConvertedInstance;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.nativelibs4java.opencl.CLMem.Usage.Input;
import static com.nativelibs4java.opencl.CLMem.Usage.Output;
import static org.bridj.Pointer.allocateDoubles;
import static org.bridj.Pointer.allocateInts;

/**
 * Created by Anton Doronin on 08.03.2017.
 * Интерфейс для выполнения вычислений
 */
public class CLInterface {

    private TreeConvertedInstance instance;

    public CLInterface(TreeConvertedInstance convertedInstance) {
        this.instance = convertedInstance;
    }

    /**
     * Выполнение расчётов на лучшем контексте выполнения
     */
    public List<Vector3d> checkIntersection(double[] beginPoints, double[] endPoints) throws IOException, InterruptedException {
        CLContext context = JavaCL.createBestContext();
        System.out.println("Max dimensions:" + context.getDevices()[0].getMaxWorkItemDimensions());
        CLQueue queue = context.createDefaultQueue();
        ByteOrder byteOrder = context.getByteOrder();
        long nodesCount = instance.getNodesArray().length;
        long planesCount = instance.getPlanesArray().length;
        Pointer<Integer> nodesPtr = allocateInts(nodesCount).order(byteOrder);
        Pointer<Double> planesPtr = allocateDoubles(planesCount).order(byteOrder);
        Pointer<Double> beginsPtr = allocateDoubles(beginPoints.length).order(byteOrder);
        Pointer<Double> endsPtr = allocateDoubles(endPoints.length).order(byteOrder);

        for (int i = 0; i < nodesCount; i++) {
            nodesPtr.set(i, instance.getNodesArray()[i]);
        }
        for (int i = 0; i < planesCount; i++) {
            planesPtr.set(i, instance.getPlanesArray()[i]);
        }

        for (int i = 0; i < beginPoints.length; i++) {
            beginsPtr.set(i, beginPoints[i]);
            endsPtr.set(i, endPoints[i]);
        }
        CLBuffer<Integer> nodesArg = context.createIntBuffer(Input, nodesPtr);
        CLBuffer<Double> planesArg = context.createDoubleBuffer(Input, planesPtr);
        CLBuffer<Double> begins = context.createDoubleBuffer(Input, beginsPtr);
        CLBuffer<Double> ends = context.createDoubleBuffer(Input, endsPtr);

        CLBuffer<Double> out = context.createDoubleBuffer(Output, allocateDoubles(beginPoints.length).order(byteOrder));


        String source = IOUtils.readText(CLInterface.class.getResource("RayCaster.cl"));
        CLProgram program = context.createProgram(source);
        //program.addBuildOption("-g");

        CLKernel kernel = program.createKernel("check_intersection");

        kernel.setArgs(begins, ends, (long) beginPoints.length / 3, nodesArg, planesArg, instance.countCells(), out);
        String mxBeanName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = mxBeanName.substring(0, mxBeanName.indexOf("@"));
        System.out.println("sudo gdb --tui --pid=" + pid);
        System.out.println("sudo ddd --debugger \"gdb --pid=" + pid + "\"");

        long before = System.nanoTime();
        long raysNum = beginPoints.length / 3;


        System.out.println("Preferred multiplier: " + kernel.getPreferredWorkGroupSizeMultiple());
        CLEvent event = kernel.enqueueNDRange(queue, new int[]{(int) raysNum});
        List<Vector3d> points = new ArrayList<>();
        long tic = System.nanoTime();
        event.setCompletionCallback(i -> System.out.println("Waited " + (System.nanoTime() - tic)/1000000));
        while(event.getCommandExecutionStatusValue() != 0) {
            Thread.sleep(20);
        }
        System.out.println("Status:" + CLEvent.CommandExecutionStatus.getEnum(event.getCommandExecutionStatus().value()));
        Pointer<Double> results = out.read(queue, event);
        for (int i = 0; i < raysNum; i++) {
            double x = results.get(i * 3);
            double y = results.get(i * 3 + 1);
            double z = results.get(i * 3 + 2);
            points.add(new Vector3d(x, y, z));
        }
        System.out.println("OpenCL worked: " + (System.nanoTime() - before) / 1000000 + " ms");
        points.size();
        program.release();
        return points;
    }
}
