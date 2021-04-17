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
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.haps.headers.BigSmallDCBroker;
import org.cloudsimplus.util.Log;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import static java.util.Comparator.comparingLong;

public class BigHAPS {

    private static final int NUMBER_OF_BROKERS = 5;
    private static final int SCHEDULING_INTERVAL = 10;

    private double MAX_HAPS_POWER_WATTS_SEC = 1050;
    private double HAPS_STATIC_POWER_WATTS_SEC = 350;

    private final int NUMBER_OF_HAPS;

    private int HOST_HAPS_NUMBER;
    private final int HOST_HAPS_PES_NUMBER = 250;
    private final long mipsHAPSHost;
    private final long ramHAPSHost;
    private final long storageHAPSHost;
    private final long bwHAPSHost;

    private final int VMS_HAPS_NUMBER;
    private final int VM_HAPS_PES_NUMBER = 250;
    private final int mipsHAPSVm;
    private final long sizeHAPSVm;
    private final int ramHAPSVm;
    private final long bwHAPSVm;

    // Properties of CLOUDLETS
    private static int NUMBER_OF_CLOUDLETS;
    private static int numberOfCloudletPerBroker;
    //long lengthCLOUDLETS = 28754000;

    private final CloudSim simulation;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final List<Datacenter> datacenterList;
    private final List<DatacenterBroker> brokers;
    private static final List<BigHAPS> simulationList = new ArrayList<>();
    private static final List<Integer> cloudletNumbers = new ArrayList<>();
    private static final List<Integer> delayNumbers = new ArrayList<>();
    private static final List<String> utilizationList = new ArrayList<String>();
    private static final List<Integer> totalUpTimeList = new ArrayList<Integer>();
    private static final List<Double> lengthMeans = new ArrayList<>();
    private static char testType;
    private static char utilizationType;
    private static boolean errorBars = false;
    private static int delay;
    private static boolean specWrite = false;

    public static void main(String[] args) throws IOException {
//        PrintStream out = new PrintStream(new FileOutputStream("table.txt"));
//        System.setOut(out);
        Scanner in = new Scanner(System.in);
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
            File file1 = new File("bigErrorBarCloudlet.txt");
            File file2 = new File("bigErrorBarVm.txt");
            if (file1.delete()) {
            }
            if(file2.delete()){
            }
        }
        if (s.equals("c")){
            testType = 'c';
            for(int i=0; i<25 ;i++){
                if(i!=0){
                    NUMBER_OF_CLOUDLETS += 100;
                }
                else{
                    NUMBER_OF_CLOUDLETS = 500;
                }
                numberOfCloudletPerBroker = NUMBER_OF_CLOUDLETS / NUMBER_OF_BROKERS;
                if(n.equals("y")){
                    errorBars = true;
                    for(int h=0; h < number_of_tests; h++){
                        simulationList.add(new BigHAPS());
                        cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
                    }
                }
                else{
                    simulationList.add(new BigHAPS());
                    cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
                }

            }
        }
        else if (s.equals("v")){
            testType = 'v';
            NUMBER_OF_CLOUDLETS = 2000;
            numberOfCloudletPerBroker = NUMBER_OF_CLOUDLETS / NUMBER_OF_BROKERS;
            for(int i=0; i<25; i++){
                if(i != 0){
                    delay += 5000;
                }
                else {
                    delay = 500;
                }
                if(n.equals("y")){
                    errorBars = true;
                    for(int h=0; h < number_of_tests; h++){
                        simulationList.add(new BigHAPS());
                        cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
                        delayNumbers.add(delay);
                    }
                }
                else{
                    simulationList.add(new BigHAPS());
                    cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
                    delayNumbers.add(delay);
                }

            }
        }

        try(BufferedWriter br = new BufferedWriter(testType == 'c' ?
                new FileWriter("bigHAPSOnlyNumbers_Cloudlet.txt",false) :
                new FileWriter("bigHAPSOnlyNumbers_VmLifeTime.txt",false))) {
        }
        /*Log.setLevel(Level.OFF);

        //Enable different log levels for specific classes of objects
        Log.setLevel(DatacenterBroker.LOGGER, Level.OFF);
        Log.setLevel(Datacenter.LOGGER, Level.OFF);
        Log.setLevel(VmScheduler.LOGGER, Level.OFF);
        Log.setLevel(VmAllocationPolicySimple.LOGGER, Level.OFF);*/

        simulationList.parallelStream().forEach(BigHAPS::run);
        simulationList.forEach(BigHAPS::printResults);
        System.out.println(utilizationList);
        System.out.println(totalUpTimeList);
        System.out.println(lengthMeans);

    }

    public void run() {
        simulation.start();
    }

    private BigHAPS() {

        NUMBER_OF_HAPS = 5;
        HOST_HAPS_NUMBER = NUMBER_OF_HAPS;
        VMS_HAPS_NUMBER = HOST_HAPS_NUMBER;

        if(NUMBER_OF_CLOUDLETS > 2000 && testType == 'c'){
            mipsHAPSHost = 1000 + (NUMBER_OF_CLOUDLETS-1000)/10;
        }
        else {
            mipsHAPSHost = 1000;
        }
        ramHAPSHost = 66000 * 10; //in Megabytes
        storageHAPSHost = 10000000 * 10;
        bwHAPSHost = 10000 * 10;

        if(NUMBER_OF_CLOUDLETS > 2000 && testType == 'c'){
            mipsHAPSVm = 1000 + (NUMBER_OF_CLOUDLETS-1000)/10;
        }
        else{
            mipsHAPSVm = 1000;
        }
        ramHAPSVm = 66000 * 10;
        sizeHAPSVm = 10000000 * 10;
        bwHAPSVm = 10000 * 10;

        simulation = new CloudSim();
        this.vmList = new ArrayList<>(VMS_HAPS_NUMBER);
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
            BigSmallDCBroker broker = new BigSmallDCBroker(simulation,"",numberOfCloudletPerBroker,1);
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
        final PowerModelHost powerModel = new PowerModelHostSimple(MAX_HAPS_POWER_WATTS_SEC, HAPS_STATIC_POWER_WATTS_SEC);
        final Host host = new  HostSimple(ramHAPSHost, bwHAPSHost, storageHAPSHost, peList);
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
            Vm vm = createVm(i++);
            vmList.add(vm);
            broker.submitVm(vm);
        }
        // Assigning Cloudlets


        i=0;
        for (DatacenterBroker broker : brokers) {
            for (; i<NUMBER_OF_CLOUDLETS; i++) {
                //RandomGenerator rg = new JDKRandomGenerator();
                ExponentialDistribution lengDist = new ExponentialDistribution(28754000);
                long lengthCLOUDLETS = (long) (lengDist.sample()*0.09325 + lengDist.sample()*0.22251 + lengDist.sample()*0.68424);
                Cloudlet cloudlet;
                if(testType == 'v'){
                    cloudlet = createCloudlet(i, lengthCLOUDLETS, delay);
                }
                else{
                    cloudlet = createCloudlet(i, lengthCLOUDLETS, 2200);
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
            fileSize = 300;
            outputSize = 300;
            pesNumber = 1;
            utilizationModel = new UtilizationModelDynamic(0.2);
            cloudlet = new CloudletSimple(id, length, pesNumber)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(new UtilizationModelFull())
                    .setUtilizationModelBw(utilizationModel)
                    .setUtilizationModelRam(utilizationModel);
        }
        else if(utilizationType == 'g'){
            if(id % numberOfCloudletPerBroker < numberOfCloudletPerBroker/2){ //LOW
                fileSize = 100;
                outputSize = 100;
                pesNumber = 1;
                utilizationModel = new UtilizationModelDynamic(Math.abs(r.nextGaussian()+5)/100);
                cloudlet = new CloudletSimple(id, length, pesNumber)
                        .setFileSize(fileSize)
                        .setOutputSize(outputSize)
                        .setUtilizationModelCpu(new UtilizationModelFull())
                        .setUtilizationModelBw(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,10)) // 10 MB
                        .setUtilizationModelRam(new UtilizationModelDynamic(UtilizationModel.Unit.ABSOLUTE,125)); // 125 MB
            }
            else if(numberOfCloudletPerBroker/2 <= id % numberOfCloudletPerBroker &&
                    id % numberOfCloudletPerBroker < numberOfCloudletPerBroker * 0.8 ){  // Mid
                fileSize = 500;
                outputSize = 500;
                pesNumber = 5;
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
        cloudlet.setSubmissionDelay(delayTime);
        cloudlet.setExecStartTime(delayTime);
        return cloudlet;
    }

    private void printResults() {
        double meanExecTime2 = 0.0;
        for (DatacenterBroker broker : brokers) {

            final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
            final Comparator<Cloudlet> hostComparator = comparingLong(cl -> cl.getVm().getHost().getId());
            finishedCloudlets.sort(hostComparator.thenComparing(cl -> cl.getVm().getId()));

            new CloudletsTableBuilder(finishedCloudlets).build();

            double meanExecTime = 0.0;
            for(int i=0; i< finishedCloudlets.size(); i++){
                meanExecTime += finishedCloudlets.get(i).getFinishTime() - finishedCloudlets.get(i).getExecStartTime();
            }
            meanExecTime /= finishedCloudlets.size();
            meanExecTime2 += meanExecTime;

        }
        System.out.println(meanExecTime2/NUMBER_OF_BROKERS);
        lengthMeans.add(meanExecTime2/NUMBER_OF_BROKERS);

        Double TotalPowerConsumptionInKWatt = 0.0;
        Double totalUtilization = 0.0;
        Double totalUpTime = 0.0;
        for (Vm vm : vmList) {
            final HostResourceStats cpuStats = vm.getHost().getCpuUtilizationStats();
            final double utilizationPercentMean = cpuStats.getMean();

            if(utilizationPercentMean > 0) {
                TotalPowerConsumptionInKWatt += vm.getHost().getPowerModel().getPower(utilizationPercentMean) * vm.getHost().getTotalUpTime() / 1000;
                totalUtilization += utilizationPercentMean;
                totalUpTime += vm.getHost().getTotalUpTime();
            }

        }
        totalUtilization /= vmList.size();
        totalUpTime /= vmList.size();
        DecimalFormat df = new DecimalFormat("#.#####");
        utilizationList.add(df.format(totalUtilization));
        totalUpTimeList.add(totalUpTime.intValue());


        //All informations
        if(!specWrite){
            try(BufferedWriter br = new BufferedWriter(testType == 'c' ?
                    new FileWriter("bigHAPS_Cloudlet.txt",false) : new FileWriter("bigHAPS_VmLifeTime.txt",false))){
                br.write("Number of Brokers : " + NUMBER_OF_BROKERS);
                br.newLine();
                br.write("Number of HAPS : "+ NUMBER_OF_HAPS);
                br.newLine();
                if(testType == 'v'){
                    br.write("Number of CloudLets: " + NUMBER_OF_CLOUDLETS);
                    br.newLine();
                }
                br.write("MAX_HAPS_POWER_WATTS_SEC: "+ MAX_HAPS_POWER_WATTS_SEC + " HAPS_STATIC_POWER_WATTS_SEC: " + HAPS_STATIC_POWER_WATTS_SEC);
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
            }
            catch (IOException e) {
                System.out.println("Unable to read file ");
            }
            specWrite = true;
        }



        try(BufferedWriter br = new BufferedWriter(testType == 'c' ?
                new FileWriter("bigHAPS_Cloudlet.txt",true) : new FileWriter("bigHAPS_VmLifeTime.txt",true))) {
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
                new FileWriter("bigHAPSOnlyNumbers_Cloudlet.txt",true) : new FileWriter("bigHAPSOnlyNumbers_VmLifeTime.txt",true))) {
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
                    new FileWriter("bigErrorBarCloudlet.txt",true) : new FileWriter("bigErrorBarVm.txt",true))) {
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
