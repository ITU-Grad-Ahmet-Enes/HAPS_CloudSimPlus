package org.cloudsimplus.haps;

import ch.qos.logback.classic.Level;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.HostResourceStats;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmResourceStats;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.builders.tables.TextTableColumn;
import org.cloudsimplus.haps.headers.BigSmallDCBroker;
import org.cloudsimplus.haps.headers.VmsTableBuilder;
import org.cloudsimplus.util.Log;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.IntStream;
import java.lang.Object;

import static java.util.Comparator.comparingLong;

public class VmPerCloudlet {
    private static int NUMBER_OF_BROKERS;
    private static final int SCHEDULING_INTERVAL = 10;

    private double MAX_HAPS_POWER_WATTS_SEC_Big = 1050;
    private double HAPS_STATIC_POWER_WATTS_SEC_Big = 350;

    private double MAX_HAPS_POWER_WATTS_SEC_Small = 105;
    private double HAPS_STATIC_POWER_WATTS_SEC_Small = 35;

    private static int NUMBER_OF_HAPS;

    private int HOST_HAPS_NUMBER;
    private int HOST_HAPS_PES_NUMBER;
    private long mipsHAPSHost;
    private long ramHAPSHost;
    private long storageHAPSHost;
    private long bwHAPSHost;

    private int VMS_HAPS_NUMBER;
    private int VM_HAPS_PES_NUMBER;
    private int mipsHAPSVm;
    private long sizeHAPSVm;
    private int ramHAPSVm;
    private long bwHAPSVm;

    // Properties of CLOUDLETS
    private static int NUMBER_OF_CLOUDLETS;
    private static int numberOfCloudletPerBroker;
    //long lengthCLOUDLETS = 28754000;

    private final CloudSim simulation;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final List<Datacenter> datacenterList;
    private final List<DatacenterBroker> brokers;
    private static final List<VmPerCloudlet> simulationList = new ArrayList<>();
    private static final List<Integer> cloudletNumbers = new ArrayList<>();
    private static final List<Integer> delayNumbers = new ArrayList<>();
    private static final List<String> utilizationList = new ArrayList<String>();
    private static final List<Integer> totalUpTimeList = new ArrayList<Integer>();
    private static final List<Double> lengthMeans = new ArrayList<>();
    private static char testType;
    private static char utilizationType;
    private static String typeHAPS;
    private static boolean errorBars = false;
    private static int delay;
    private static boolean specWrite = false;
    private Double TotalPowerConsumptionInKWatt = 0.0;
    private Double totalUtilization = 0.0;
    private Double totalUpTime = 0.0;
    private static int vmPerCloudlet;

    public static void main(String[] args) throws IOException {
//        Random random = new Random();
//        PrintStream out = new PrintStream(new FileOutputStream("table" +random.nextInt(250) + ".txt"));
//        System.setOut(out);
        Scanner in = new Scanner(System.in);
        System.out.println("For Big HAPS Enter b , For Small HAPS Enter s !");
        typeHAPS = in.nextLine();
        if(typeHAPS.equals("b") || typeHAPS.equals("s")){
            if(typeHAPS.equals("b")){
                NUMBER_OF_BROKERS = 5;
                NUMBER_OF_HAPS = NUMBER_OF_BROKERS;
            }
            else {
                NUMBER_OF_BROKERS = 50;
                NUMBER_OF_HAPS = NUMBER_OF_BROKERS;
            }
            System.out.println("For Vm LifeTime Enter v , For CloudLetNumbers Enter c !");
            String s = in.nextLine();
            System.out.println("For Normal Utilization Enter o , For Gauss Utilization Enter g !");
            String y = in.nextLine();
            utilizationType = y.equals("o") ? 'o': 'g';
            System.out.println("Do you want error bars? If yes enter enter y ");
            String n = in.nextLine();
            int number_of_tests = 0;
            if(n.equals("y")) {
                System.out.println("Enter number of test! ");
                number_of_tests = in.nextInt();
                File file;
                if(typeHAPS.equals("b")){
                    if(s.equals("c")){
                        file = new File("bigErrorBarCloudlet.txt");
                    }
                    else{
                        file = new File("bigErrorBarVm.txt");
                    }
                }
                else{
                    if(s.equals("c")){
                        file = new File("smallErrorBarCloudlet.txt");
                    }
                    else{
                        file = new File("smallErrorBarVm.txt");
                    }
                }
                if (file.delete() ) {
                }
            }
            System.out.println("Enter vmPerCloudlet");
            vmPerCloudlet = in.nextInt();
            if (s.equals("c")){
                testType = 'c';
                for(int i=0; i<24 ;i++){
                    if(i!=0){
                        NUMBER_OF_CLOUDLETS += 200;
                    }
                    else{
                        NUMBER_OF_CLOUDLETS = 200;
                    }
                    numberOfCloudletPerBroker = NUMBER_OF_CLOUDLETS / NUMBER_OF_BROKERS;
                    if(n.equals("y")){
                        errorBars = true;
                        for(int h=0; h < number_of_tests; h++){
                            simulationList.add(new VmPerCloudlet());
                            cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
                        }
                    }
                    else{
                        simulationList.add(new VmPerCloudlet());
                        cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
                    }

                }
            }
            else if (s.equals("v")){
                testType = 'v';
                NUMBER_OF_CLOUDLETS = 4000;
                numberOfCloudletPerBroker = NUMBER_OF_CLOUDLETS / NUMBER_OF_BROKERS;
                for(int i=0; i<20; i++){
                    if(i != 0){
                        delay += 500;
                    }
                    else {
                        delay = 50;
                    }
                    if(n.equals("y")){
                        errorBars = true;
                        for(int h=0; h < number_of_tests; h++){
                            simulationList.add(new VmPerCloudlet());
                            cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
                            delayNumbers.add(delay);
                        }
                    }
                    else{
                        simulationList.add(new VmPerCloudlet());
                        cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
                        delayNumbers.add(delay);
                    }

                }
            }

            try(BufferedWriter br = new BufferedWriter(testType == 'c' ?
                    typeHAPS.equals("b") ?
                        new FileWriter("bigHAPSOnlyNumbers_Cloudlet.txt",false) :
                        new FileWriter("smallHAPSOnlyNumbers_Cloudlet.txt",false) :
                    typeHAPS.equals("b") ?
                        new FileWriter("bigHAPSOnlyNumbers_VmLifeTime.txt",false) :
                        new FileWriter("smallHAPSOnlyNumbers_VmLifeTime.txt",false) )) {
            }

            /*Log.setLevel(Level.OFF);
            Log.setLevel(DatacenterBroker.LOGGER, Level.OFF);
            Log.setLevel(Datacenter.LOGGER, Level.OFF);
            Log.setLevel(VmScheduler.LOGGER, Level.OFF);
            Log.setLevel(VmAllocationPolicySimple.LOGGER, Level.OFF);*/

            simulationList.parallelStream().forEach(VmPerCloudlet::run);
            simulationList.forEach(VmPerCloudlet::printResults);
            System.out.println(utilizationList);
            System.out.println(totalUpTimeList);
            System.out.println(lengthMeans);
            System.out.println(typeHAPS);
        }

    }

    public void run() {
        simulation.start();
    }

    private VmPerCloudlet() {

        if(typeHAPS.equals("b")){
            HOST_HAPS_NUMBER = NUMBER_OF_HAPS;
            HOST_HAPS_PES_NUMBER = 10;
            VM_HAPS_PES_NUMBER = 10;

            mipsHAPSHost = 1000 * 10;
            ramHAPSHost = 100000 * 10; //in Megabytes
            storageHAPSHost = 10000000 * 10;
            bwHAPSHost = 10000 * 10;

            mipsHAPSVm = 10*vmPerCloudlet;
            ramHAPSVm = 100000 * 10 / numberOfCloudletPerBroker;
            sizeHAPSVm = 10000000 * 10 / numberOfCloudletPerBroker;
            bwHAPSVm = 10000 * 10 / numberOfCloudletPerBroker;
        }
        else{
            HOST_HAPS_NUMBER = NUMBER_OF_HAPS;
            HOST_HAPS_PES_NUMBER = 10;
            VM_HAPS_PES_NUMBER = 10;

            mipsHAPSHost = 1000;
            ramHAPSHost = 100000;
            storageHAPSHost = 10000000;
            bwHAPSHost = 10000;

            mipsHAPSVm = 10*vmPerCloudlet;
            ramHAPSVm = 100000 / numberOfCloudletPerBroker * vmPerCloudlet;
            sizeHAPSVm = (long) 10000000 / numberOfCloudletPerBroker * vmPerCloudlet;
            bwHAPSVm = (long) 10000 / numberOfCloudletPerBroker * vmPerCloudlet;
        }


//500 VM limit koy
        simulation = new CloudSim();
        this.vmList = new ArrayList<>(NUMBER_OF_CLOUDLETS);
        this.cloudletList = new ArrayList<>(NUMBER_OF_CLOUDLETS);
        this.datacenterList = new ArrayList<>();
        this.brokers = createBrokers(0.0);

        createDatacenter();
        createVmsAndCloudlets();
        //simulation.start();
        //printResults();
    }


    private List<DatacenterBroker> createBrokers(double lambda) {
        final List<DatacenterBroker> list = new ArrayList<>(NUMBER_OF_BROKERS);
        for(int i = 0; i < NUMBER_OF_BROKERS; i++) {
            BigSmallDCBroker broker = new BigSmallDCBroker(simulation,"",numberOfCloudletPerBroker, vmPerCloudlet);
            broker.setLambdaValue(lambda);
            list.add(broker);
        }
        return list;
    }

    /**
     * Creates a Datacenter and its Hosts.
     */
    private void createDatacenter() {
        for(int i=0; i<NUMBER_OF_HAPS; i++) {
            final List<Host> hostList = new ArrayList<>();
            hostList.add(createHost(i));
            Datacenter datacenter = new DatacenterSimple(simulation,hostList, new VmAllocationPolicySimple());
            datacenter.setSchedulingInterval(SCHEDULING_INTERVAL);
            datacenterList.add(datacenter);
        }
    }

    private Host createHost(int id) {
        final List<Pe> peList = new ArrayList<>();
        for(int i = 0; i < HOST_HAPS_PES_NUMBER; i++){
            peList.add(new PeSimple(mipsHAPSHost, new PeProvisionerSimple()));
        }

        PowerModelHost powerModel;
        if(typeHAPS.equals("b")){
            powerModel = new PowerModelHostSimple(MAX_HAPS_POWER_WATTS_SEC_Big, HAPS_STATIC_POWER_WATTS_SEC_Big);
        }
        else {
            powerModel = new PowerModelHostSimple(MAX_HAPS_POWER_WATTS_SEC_Small, HAPS_STATIC_POWER_WATTS_SEC_Small);
        }

        final Host host = new HostSimple(ramHAPSHost, bwHAPSHost, storageHAPSHost, peList);
        host    .setRamProvisioner(new ResourceProvisionerSimple())
                .setBwProvisioner(new ResourceProvisionerSimple())
                .setVmScheduler(new VmSchedulerTimeShared())
                .setPowerModel(powerModel);
        host.enableUtilizationStats();
        return  host;
    }

    private void createVmsAndCloudlets() {
        // Assigning Vms
        int i=0;
        for (DatacenterBroker broker : brokers) {
            for(int j=0; j<numberOfCloudletPerBroker/vmPerCloudlet; j++){
                Vm vm = createVm(i++);
                vmList.add(vm);
                broker.submitVm(vm);
            }
        }
        // Assigning Cloudlets


        i=0;
        for (DatacenterBroker broker : brokers) {
            for (; i<NUMBER_OF_CLOUDLETS; i++) {
                //RandomGenerator rg = new JDKRandomGenerator();
                ExponentialDistribution lengDist = new ExponentialDistribution(28754*2);
                long lengthCLOUDLETS = (long) (lengDist.sample()*0.09325 + lengDist.sample()*0.22251 + lengDist.sample()*0.68424);
                if(lengthCLOUDLETS > 80000) lengthCLOUDLETS = 80000;
                if(lengthCLOUDLETS < 20000) lengthCLOUDLETS = 20000;
                Cloudlet cloudlet;
                if(testType == 'v'){
                    cloudlet = createCloudlet(i, lengthCLOUDLETS, delay);
                }
                else{
                    cloudlet = createCloudlet(i, lengthCLOUDLETS, 220);
                }
                cloudletList.add(cloudlet);
                broker.submitCloudlet(cloudlet);
                if((i+1)%numberOfCloudletPerBroker == 0) {
                    i++;
                    break;
                }
            }
        }
    }

    private Vm createVm(int id) {
        Vm vm = new VmSimple(id, mipsHAPSVm, VM_HAPS_PES_NUMBER)
                .setRam(ramHAPSVm).setBw(bwHAPSVm).setSize(sizeHAPSVm)
                .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        vm.enableUtilizationStats();
        return vm;
    }



    private Cloudlet createCloudlet(long id, long length, int delay) {
        long fileSize;
        long outputSize;
        int pesNumber;
        UtilizationModel utilizationModel;
        Cloudlet cloudlet = null;
        Random r = new Random();
        if(utilizationType == 'o'){
            fileSize = 600;
            outputSize = 600;
            pesNumber = 10;
            utilizationModel = new UtilizationModelDynamic(0.2);
            cloudlet = new CloudletSimple(id, length, pesNumber)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(new UtilizationModelFull())
                    .setUtilizationModelBw(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,75))
                    .setUtilizationModelRam(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,750));
        }
        else if(utilizationType == 'g'){
            if(id % numberOfCloudletPerBroker < numberOfCloudletPerBroker*0.4){ //LOW
                fileSize = 100;
                outputSize = 100;
                pesNumber = 5;
                utilizationModel = new UtilizationModelDynamic(Math.abs(r.nextGaussian()+5)/100);
                cloudlet = new CloudletSimple(id, length, pesNumber)
                        .setFileSize(fileSize)
                        .setOutputSize(outputSize)
                        .setUtilizationModelCpu(new UtilizationModelFull())
                        .setUtilizationModelBw(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,10)) // 10 MB
                        .setUtilizationModelRam(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,125)); // 125 MB
            }
            else if(numberOfCloudletPerBroker*0.4 <= id % numberOfCloudletPerBroker &&
                    id % numberOfCloudletPerBroker < numberOfCloudletPerBroker * 0.8 ){  // Mid
                fileSize = 500;
                outputSize = 500;
                pesNumber = 7;
                utilizationModel = new UtilizationModelDynamic(Math.abs(r.nextGaussian()+5)/100);
                cloudlet = new CloudletSimple(id, length, pesNumber)
                        .setFileSize(fileSize)
                        .setOutputSize(outputSize)
                        .setUtilizationModelCpu(new UtilizationModelFull())
                        .setUtilizationModelBw(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,50)) // 50 MB
                        .setUtilizationModelRam(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,250)); // 250 MB
            }
            else{ // High
                fileSize = 1000;
                outputSize = 1000;
                pesNumber = 10;
                utilizationModel = new UtilizationModelDynamic(Math.abs(r.nextGaussian()+10)/100);
                cloudlet = new CloudletSimple(id, length, pesNumber)
                        .setFileSize(fileSize)
                        .setOutputSize(outputSize)
                        .setUtilizationModelCpu(new UtilizationModelFull())
                        .setUtilizationModelBw(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,100)) // 100 MB
                        .setUtilizationModelRam(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,500)); // 500 MB
            }
        }
        ExponentialDistribution expDist = new ExponentialDistribution(delay);
        double delayTime = (expDist.sample()*0.34561 + expDist.sample()*0.08648 + expDist.sample()*0.56791);
        if(delayTime > delay * 3) delayTime = delay * 3;
        cloudlet.setSubmissionDelay(delayTime);
        cloudlet.setExecStartTime(delayTime);
        return cloudlet;
    }

    public String util(final Vm vm) {
        final VmResourceStats cpuStats = vm.getCpuUtilizationStats();
        final double utilizationPercentMean = cpuStats.getMean();
        return String.format("%.4f", utilizationPercentMean);
    }


    private void printResults() {
        double meanExecTime = 0.0;
        for (DatacenterBroker broker : brokers) {

            final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
            final List<Vm> finishedVms = broker.getVmCreatedList();
            final Comparator<Cloudlet> hostComparator = comparingLong(cl -> cl.getVm().getHost().getId());
            finishedCloudlets.sort(hostComparator.thenComparing(cl -> cl.getVm().getId()));
            finishedVms.sort(Comparator.comparingDouble(vm -> vm.getId()));

            new CloudletsTableBuilder(finishedCloudlets)
                    .build();

            new VmsTableBuilder(finishedVms)
                    .addColumn(2, new TextTableColumn("Vm Utilization "), this::util)
                    .build();

            for(int i=0; i< finishedCloudlets.size(); i++){
                meanExecTime += finishedCloudlets.get(i).getFinishTime() - finishedCloudlets.get(i).getExecStartTime();
            }
            meanExecTime /= finishedCloudlets.size();

        }
        System.out.println(meanExecTime);
        lengthMeans.add(meanExecTime);


        for(Datacenter dc : datacenterList){
            final HostResourceStats cpuStats = dc.getHost(0).getCpuUtilizationStats();
            final double utilizationPercentMean = cpuStats.getMean();
            TotalPowerConsumptionInKWatt += dc.getHost(0).getPowerModel().getPower(utilizationPercentMean) * dc.getHost(0).getTotalUpTime() / 1000;
            totalUtilization += utilizationPercentMean ;
            totalUpTime += dc.getHost(0).getTotalUpTime();

        }
        totalUtilization /= NUMBER_OF_HAPS;
        totalUpTime /= NUMBER_OF_HAPS;
        DecimalFormat df = new DecimalFormat("#.#####");
        utilizationList.add(df.format(totalUtilization));
        totalUpTimeList.add(totalUpTime.intValue());


        //All informations
        if(!specWrite){
            try(BufferedWriter br = new BufferedWriter(testType == 'c' ?
                    typeHAPS.equals("b") ?
                        new FileWriter("bigHAPS_Cloudlet.txt",false) :
                        new FileWriter("smallHAPS_Cloudlet.txt",false) :
                    typeHAPS.equals("b") ?
                        new FileWriter("bigHAPS_VmLifeTime.txt",false) :
                        new FileWriter("smallHAPS_VmLifeTime.txt",false) )){
                br.write("Number of Brokers : " + NUMBER_OF_BROKERS);
                br.newLine();
                br.write("Number of HAPS : "+ NUMBER_OF_HAPS);
                br.newLine();
                if(testType == 'v'){
                    br.write("Number of CloudLets: " + NUMBER_OF_CLOUDLETS);
                    br.newLine();
                }
                if(typeHAPS.equals("b")){
                    br.write("MAX_HAPS_POWER_WATTS_SEC: "+ MAX_HAPS_POWER_WATTS_SEC_Big + " HAPS_STATIC_POWER_WATTS_SEC: " + HAPS_STATIC_POWER_WATTS_SEC_Big);
                }
                else{
                    br.write("MAX_HAPS_POWER_WATTS_SEC: "+ MAX_HAPS_POWER_WATTS_SEC_Small + " HAPS_STATIC_POWER_WATTS_SEC: " + HAPS_STATIC_POWER_WATTS_SEC_Small);
                }
                br.newLine();
                br.write("---------------------------------------------------------------------------------------\n"+ "HAPS Stations Properties " );
                br.newLine();
                br.write("Number of Stations: " + NUMBER_OF_HAPS +
                        ", Number of Hosts: " + HOST_HAPS_NUMBER +
                        ", Number of Vms: " + VMS_HAPS_NUMBER);
                br.newLine();
                br.write("Mips for Host: " + mipsHAPSHost +
                        ", Ram for Host: " + ramHAPSHost +
                        ", Storage for Host: " + storageHAPSHost +
                        ", BW for Host: " + bwHAPSHost);
                br.newLine();
                br.write("Mips for Vm: " + mipsHAPSVm +
                        ", Size for Vm: " + sizeHAPSVm +
                        ", Ram for Vm: " + ramHAPSVm +
                        ", BW for Vm: " + bwHAPSVm + "\n");
                br.newLine();
                br.newLine();
                br.flush();
            }
            catch (IOException e) {
                System.out.println("Unable to read file ");
            }
            specWrite = true;
        }



        try(BufferedWriter br = new BufferedWriter(testType == 'c' ?
                typeHAPS.equals("b") ?
                        new FileWriter("bigHAPS_Cloudlet.txt",true) :
                        new FileWriter("smallHAPS_Cloudlet.txt",true) :
                typeHAPS.equals("b") ?
                        new FileWriter("bigHAPS_VmLifeTime.txt",true) :
                        new FileWriter("smallHAPS_VmLifeTime.txt",true))) {
            if(testType == 'c'){
                int index = simulationList.indexOf(this);
                br.write("Number of Cloudlets : "+ cloudletNumbers.get(index));
                br.newLine();
            }
            if(testType == 'v'){
                int index = simulationList.indexOf(this);
                br.write("Mean Delay: " + delayNumbers.get(index));
                br.newLine();
            }
            br.write("Total Energy Consumption is " + TotalPowerConsumptionInKWatt.intValue()  + " kW");
            br.newLine();
            br.write("Mean Utilization " + df.format(totalUtilization));
            br.newLine();
            br.write("Mean Total Up Time is " + totalUpTime.intValue() +" seconds");
            br.newLine();
            br.newLine();
            br.flush();
        }
        catch (IOException e) {
            System.out.println("Unable to read file ");
        }

        try(BufferedWriter br = new BufferedWriter(testType == 'c' ?
                typeHAPS.equals("b") ?
                        new FileWriter("bigHAPSOnlyNumbers_Cloudlet.txt",true) :
                        new FileWriter("smallHAPSOnlyNumbers_Cloudlet.txt",true) :
                typeHAPS.equals("b") ?
                        new FileWriter("bigHAPSOnlyNumbers_VmLifeTime.txt",true) :
                        new FileWriter("smallHAPSOnlyNumbers_VmLifeTime.txt",true))) {
            br.write(TotalPowerConsumptionInKWatt.intValue() + "");
            br.newLine();
            br.write(df.format(totalUtilization));
            br.newLine();
            br.write(totalUpTime.intValue()+ "");
            br.newLine();
            br.flush();
        }
        catch (IOException e) {
            System.out.println("Unable to read file ");
        }
        if(errorBars){
            try(BufferedWriter br = new BufferedWriter(testType == 'c' ?
                    typeHAPS.equals("b") ?
                        new FileWriter("bigErrorBarCloudlet.txt",true) :
                        new FileWriter("smallErrorBarCloudlet.txt",true) :
                    typeHAPS.equals("b") ?
                        new FileWriter("bigErrorBarVm.txt",true) :
                        new FileWriter("smallErrorBarVm.txt",true))) {
                br.write(TotalPowerConsumptionInKWatt.intValue() + "");
                br.newLine();
                br.write(df.format(totalUtilization));
                br.newLine();
                br.write(totalUpTime.intValue()+ "");
                br.newLine();
                br.flush();
            }
            catch (IOException e) {
                System.out.println("Unable to read file ");
            }
        }
    }
}
