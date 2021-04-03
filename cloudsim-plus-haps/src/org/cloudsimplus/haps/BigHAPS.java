package org.cloudsimplus.haps;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
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
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.HostResourceStats;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.haps.headers.BigSmallDCBroker;
import org.cloudsimplus.haps.headers.DatacenterBrokerLambda;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static java.util.Comparator.comparingLong;

public class BigHAPS {

    private static final int NUMBER_OF_BROKERS = 5;
    private static final int SCHEDULING_INTERVAL = 10;

    private double MAX_HAPS_POWER_WATTS_SEC = 500;
    private double HAPS_STATIC_POWER_WATTS_SEC = 350;

    private final int NUMBER_OF_HAPS;

    private int HOST_HAPS_NUMBER;
    private final int HOST_HAPS_PES_NUMBER = 5;
    private final long mipsHAPSHost;
    private final long ramHAPSHost;
    private final long storageHAPSHost;
    private final long bwHAPSHost;

    private final int VMS_HAPS_NUMBER;
    private final int VM_HAPS_PES_NUMBER = 5;
    private final int mipsHAPSVm;
    private final long sizeHAPSVm;
    private final int ramHAPSVm;
    private final long bwHAPSVm;

    // Properties of CLOUDLETS
    private static int NUMBER_OF_CLOUDLETS = 25;
    private static int numberOfCloudletPerBroker = NUMBER_OF_CLOUDLETS / NUMBER_OF_BROKERS;
    long lengthCLOUDLETS = 28754000;

    private final CloudSim simulation;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final List<Datacenter> datacenterList;
    private final List<DatacenterBroker> brokers;
    private static final List<BigHAPS> simulationList = new ArrayList<>();
    private static final List<Integer> cloudletNumbers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        for(int i=0; i<=200 ;i++){
            if(i==0){
                continue;
            }
            else{
                NUMBER_OF_CLOUDLETS += 25;
                numberOfCloudletPerBroker = NUMBER_OF_CLOUDLETS / NUMBER_OF_BROKERS;
            }
            simulationList.add(new BigHAPS());
            cloudletNumbers.add(NUMBER_OF_CLOUDLETS);
        }
        //new SmallHAPS();
        simulationList.parallelStream().forEach(BigHAPS::run);
        simulationList.forEach(BigHAPS::printResults);
        //new BigHAPS();
    }

    public void run() {
        simulation.start();
    }

    private BigHAPS() {

        NUMBER_OF_HAPS = 5;
        HOST_HAPS_NUMBER = NUMBER_OF_HAPS;
        VMS_HAPS_NUMBER = HOST_HAPS_NUMBER;

        mipsHAPSHost = 1000 * 10;
        ramHAPSHost = 2048 * 10;
        storageHAPSHost = 1000000 * 10;
        bwHAPSHost = 10000 * 10;

        mipsHAPSVm = 1000 * 10;
        ramHAPSVm = 2048 * 10;
        sizeHAPSVm = 1000000 * 10;
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
            BigSmallDCBroker broker = new BigSmallDCBroker(simulation,"",numberOfCloudletPerBroker);
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
                Cloudlet cloudlet = createCloudlet(i, lengthCLOUDLETS);
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



    private Cloudlet createCloudlet(long id, long length) {
        final long fileSize = 300;
        final long outputSize = 300;
        final int pesNumber = 1;
        final UtilizationModel utilizationModel = new UtilizationModelDynamic(0.2);
        Cloudlet cloudlet
                = new CloudletSimple(id, length, pesNumber)
                .setFileSize(fileSize)
                .setOutputSize(outputSize)
                .setUtilizationModelCpu(new UtilizationModelFull())
                .setUtilizationModelBw(utilizationModel)
                .setUtilizationModelRam(utilizationModel);

        RandomGenerator rg = new JDKRandomGenerator();

        ExponentialDistribution expDist = new ExponentialDistribution(rg,2200);
        double delayTime = (expDist.sample()*0.34561 + expDist.sample()*0.08648 + expDist.sample()*0.56791);
        cloudlet.setSubmissionDelay(delayTime);
        cloudlet.setExecStartTime(delayTime);
        return cloudlet;
    }

    private void printResults() {
        Double TotalPowerConsumptionInKWatt = 0.0;
        for (DatacenterBroker broker : brokers) {

            final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
            final Comparator<Cloudlet> hostComparator = comparingLong(cl -> cl.getVm().getHost().getId());
            finishedCloudlets.sort(hostComparator.thenComparing(cl -> cl.getVm().getId()));

            new CloudletsTableBuilder(finishedCloudlets).build();

            Double powerConsumptionInKWatt = 0.0;
            Map<Long,Double> datacenterEnergyConsumption = new TreeMap<>();
            for(int i = 0; i < broker.getCloudletFinishedList().size(); i++){
                DecimalFormat df = new DecimalFormat("#.##");

                final HostResourceStats cpuStats = broker.getCloudletCreatedList().get(i).getVm().getHost().getCpuUtilizationStats();
                final double utilizationPercentMean = cpuStats.getMean();
                powerConsumptionInKWatt = broker.getCloudletCreatedList().get(i).getVm().getHost().getPowerModel().getPower(utilizationPercentMean);
                powerConsumptionInKWatt = powerConsumptionInKWatt * broker.getCloudletCreatedList().get(i).getVm().getHost().getTotalUpTime();
                long datacenterID = broker.getCloudletCreatedList().get(i).getVm().getHost().getDatacenter().getId();
                datacenterEnergyConsumption.put(datacenterID,powerConsumptionInKWatt);
            }
            for(Map.Entry entry : datacenterEnergyConsumption.entrySet()) {
                DecimalFormat df = new DecimalFormat("#.##");
                TotalPowerConsumptionInKWatt += Double.parseDouble(df.format(entry.getValue()).replaceAll(",", "."));
            }
        }
        //All informations
        try(BufferedWriter br = new BufferedWriter(new FileWriter("bigHAPS.txt",true))) {
            int index = simulationList.indexOf(this);
            br.write("Number of Brokers : "+ NUMBER_OF_BROKERS);
            br.newLine();
            br.write("Number of Cloudlets : "+ cloudletNumbers.get(index));
            br.newLine();
            br.write("Number of HAPS : "+ NUMBER_OF_HAPS);
            br.newLine();
            br.write("MAX_HAPS_POWER_WATTS_SEC: "+ MAX_HAPS_POWER_WATTS_SEC + " HAPS_STATIC_POWER_WATTS_SEC: " + HAPS_STATIC_POWER_WATTS_SEC);
            br.newLine();
            br.write("Total Energy Consumption is " + TotalPowerConsumptionInKWatt.intValue() / 1000 + " kW");
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
            br.flush();
        } catch (IOException e) {
            System.out.println("Unable to read file ");
        }
        //Only Numbers
        try(BufferedWriter br = new BufferedWriter(new FileWriter("bigHAPSOnlyNumbers.txt",true))) {
            br.write(TotalPowerConsumptionInKWatt.intValue() / 1000 + "");
            br.newLine();
            br.flush();
        } catch (IOException e) {
            System.out.println("Unable to read file ");
        }
    }
}
