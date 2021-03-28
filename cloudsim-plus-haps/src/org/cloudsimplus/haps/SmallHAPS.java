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
import org.cloudsimplus.haps.headers.BigSmallDCBroker;
import org.cloudsimplus.haps.headers.DatacenterBrokerLambda;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class SmallHAPS {

    private static final int NUMBER_OF_BROKERS = 25;
    private static final int SCHEDULING_INTERVAL = 10;

    private double MAX_HAPS_POWER_WATTS_SEC = 50;
    private double HAPS_STATIC_POWER_WATTS_SEC = 35;

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
    private static final int NUMBER_OF_CLOUDLETS = 100;
    long lengthCLOUDLETS = 10000;

    private final CloudSim simulation;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final List<Datacenter> datacenterList;
    private final List<DatacenterBroker> brokers;
    private static Map<Double, Map<Long, Double>> brokerLambdaEnergyConsumption;

    public static void main(String[] args) throws IOException {
        new SmallHAPS();
    }

    private SmallHAPS() {

        NUMBER_OF_HAPS = 25;
        HOST_HAPS_NUMBER = 25;
        VMS_HAPS_NUMBER = HOST_HAPS_NUMBER;

        mipsHAPSHost = 1000;
        ramHAPSHost = 2048;
        storageHAPSHost = 1000000;
        bwHAPSHost = 10000;

        mipsHAPSVm = 1000;
        ramHAPSVm = 2048;
        sizeHAPSVm = 1000000;
        bwHAPSVm = 10000;

        simulation = new CloudSim();
        this.vmList = new ArrayList<>(VMS_HAPS_NUMBER);
        this.cloudletList = new ArrayList<>(NUMBER_OF_CLOUDLETS);
        this.datacenterList = new ArrayList<>();
        this.brokers = createBrokers(0.0);
        brokerLambdaEnergyConsumption = new TreeMap<>();

        createDatacenter();
        createVmsAndCloudlets();
        simulation.start();

        printResultsOnlyNumbers();
        printResults();

    }


    private List<DatacenterBroker> createBrokers(double lamda) {
        final List<DatacenterBroker> list = new ArrayList<>(NUMBER_OF_BROKERS);
        for(int i = 0; i < NUMBER_OF_BROKERS; i++) {
            BigSmallDCBroker broker = new BigSmallDCBroker(simulation,"",NUMBER_OF_HAPS/5,VMS_HAPS_NUMBER/5);
            broker.setLambdaValue(lamda);
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
            //for(; i<VMS_HAPS_NUMBER;i++) {

                Vm vm = createVm(i++);
                vmList.add(vm);
                broker.submitVm(vm);
                //if((i+1)%5 == 0)
                    //break;
            //}
        }

        // Assigning Cloudlets
        i=0;
        for (DatacenterBroker broker : brokers) {
            for (; i<NUMBER_OF_CLOUDLETS; i++) {
                Cloudlet cloudlet = createCloudlet(i, lengthCLOUDLETS);
                cloudletList.add(cloudlet);
                broker.submitCloudlet(cloudlet);
                if((i+1)%5 == 0) {
                    i++;
                    break;
                }

            }
        }
    }

    private List<Cloudlet> createAndSubmitCloudlets(DatacenterBroker broker) {
        final List<Cloudlet> list = new ArrayList<>(NUMBER_OF_CLOUDLETS);
        long cloudletId;
        for(long i = (broker.getId()-1) * NUMBER_OF_CLOUDLETS; i < NUMBER_OF_CLOUDLETS*broker.getId(); i++){
            cloudletId = i;
            Cloudlet cloudlet = createCloudlet(cloudletId,lengthCLOUDLETS);
            list.add(cloudlet);
        }
        broker.submitCloudletList(list);
        return list;
    }

    private void createAndSubmitVms(DatacenterBroker broker) {

        for(int i=0; i<VMS_HAPS_NUMBER;i++) {
            for (int j=0; j<5; j++) {
                Vm vm = createVm(i);
                vmList.add(vm);
                broker.submitVm(vm);
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
        double delayTime = expDist.sample();
        cloudlet.setSubmissionDelay(delayTime);
        cloudlet.setExecStartTime(delayTime);
        return cloudlet;
    }

    private void printResultsOnlyNumbers(){
        for (DatacenterBroker broker : brokers) {

            //TEST AMACLI
            /*for(Vm vm : vmList){
                final HostResourceStats cpuStats = vm.getHost().getCpuUtilizationStats();
                final double utilizationPercentMean = cpuStats.getMean();
                Double powerConsumptionInKWatt = vm.getHost().getPowerModel().getPower(utilizationPercentMean);
                System.out.println(powerConsumptionInKWatt);
            }*/

            Double powerConsumptionInKWatt = 0.0;
            Map<Long,Double> datacenterEnergyConsumption = new TreeMap<>();
            for(int i = 0; i < broker.getCloudletFinishedList().size(); i++){
                DecimalFormat df = new DecimalFormat("#.##");

                final HostResourceStats cpuStats = broker.getCloudletCreatedList().get(i).getVm().getHost().getCpuUtilizationStats();
                final double utilizationPercentMean = cpuStats.getMean();
                powerConsumptionInKWatt = broker.getCloudletCreatedList().get(i).getVm().getHost().getPowerModel().getPower(utilizationPercentMean);

                long datacenterID = broker.getCloudletCreatedList().get(i).getVm().getHost().getDatacenter().getId();
                datacenterEnergyConsumption.put(datacenterID,powerConsumptionInKWatt);
            }

            Double TotalPowerConsumptionInKWatt = 0.0;
            for(Map.Entry entry : datacenterEnergyConsumption.entrySet()) {
                DecimalFormat df = new DecimalFormat("#.##");
                TotalPowerConsumptionInKWatt += Double.parseDouble(df.format(entry.getValue()).replaceAll(",", "."));
            }

            List<Cloudlet> sortedFinishedCloudletList;
            sortedFinishedCloudletList = broker.getCloudletFinishedList();
            sortedFinishedCloudletList.sort(Comparator.comparingDouble(Cloudlet::getActualCpuTime));

            DecimalFormat df = new DecimalFormat("#.##");

            if(brokerLambdaEnergyConsumption.containsKey(((DatacenterBrokerLambda) broker).getLambdaValue())) {
                brokerLambdaEnergyConsumption.get(((DatacenterBrokerLambda) broker).getLambdaValue()).put(broker.getId(), Double.valueOf(df.format(TotalPowerConsumptionInKWatt).replaceAll(",", ".")));
            } else {
                Map<Long,Double> brokerEnergyConsumption = new TreeMap<>();
                brokerEnergyConsumption.put(broker.getId(), Double.valueOf(df.format(TotalPowerConsumptionInKWatt).replaceAll(",", ".")));
                brokerLambdaEnergyConsumption.put(((DatacenterBrokerLambda) broker).getLambdaValue(), brokerEnergyConsumption);
            }

            //if(((DatacenterBrokerLambda) broker).getLambdaValue() == 1.0) {
            if(brokerLambdaEnergyConsumption.size() == 11){
                if(brokerLambdaEnergyConsumption.get(1.0).size() == NUMBER_OF_BROKERS) {
                    try(BufferedWriter br = new BufferedWriter(new FileWriter("outputOnlyNumbersEnergyPower.txt",true))) {
                        //br.newLine();

                        // First Base Properties
                        br.write(MAX_HAPS_POWER_WATTS_SEC + "\n");
                        for(Map.Entry entry : brokerLambdaEnergyConsumption.entrySet()) {

                            for(Map.Entry value : ((Map<Long, Integer>)entry.getValue()).entrySet()) {
                                br.write("" + value.getValue());
                                br.newLine();
                            }
                        }
                        br.flush();
                        brokerLambdaEnergyConsumption.clear();
                    } catch (IOException e) {
                        System.out.println("Unable to read file ");
                    }
                }
            }
        }
    }

    private void printResults() {
        for (DatacenterBroker broker : brokers) {

            /*final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
            final Comparator<Cloudlet> hostComparator = comparingLong(cl -> cl.getVm().getHost().getId());
            finishedCloudlets.sort(hostComparator.thenComparing(cl -> cl.getVm().getId()));

            new CloudletsTableBuilder(finishedCloudlets).build();*/

            Double powerConsumptionInKWatt = 0.0;
            Map<Long,Double> datacenterEnergyConsumption = new TreeMap<>();
            for(int i = 0; i < broker.getCloudletFinishedList().size(); i++){
                DecimalFormat df = new DecimalFormat("#.##");

                final HostResourceStats cpuStats = broker.getCloudletCreatedList().get(i).getVm().getHost().getCpuUtilizationStats();
                final double utilizationPercentMean = cpuStats.getMean();
                powerConsumptionInKWatt = broker.getCloudletCreatedList().get(i).getVm().getHost().getPowerModel().getPower(utilizationPercentMean);
                /*System.out.printf(
                        "\tHost %d CPU Usage mean: %6.1f%% | Power Consumption mean: %8.0f W%n",
                        broker.getCloudletCreatedList().get(i).getVm().getHost().getId(), utilizationPercentMean * 100, watts);*/

                //System.out.println(broker.getCloudletCreatedList().get(i).getVm().getHost().po.getDatacenter().getPowerModel().getPowerMeasurement());

                //powerConsumptionInKWatt = Double.parseDouble(df.format(broker.getCloudletCreatedList().get(i).getVm().getHost().getDatacenter().getPowerModel().getPowerMeasurement()).replaceAll(",", "."));
                long datacenterID = broker.getCloudletCreatedList().get(i).getVm().getHost().getDatacenter().getId();
                datacenterEnergyConsumption.put(datacenterID,powerConsumptionInKWatt);
            }

            Double TotalPowerConsumptionInKWatt = 0.0;
            for(Map.Entry entry : datacenterEnergyConsumption.entrySet()) {
                DecimalFormat df = new DecimalFormat("#.##");
                TotalPowerConsumptionInKWatt += Double.parseDouble(df.format(entry.getValue()).replaceAll(",", "."));
            }

            List<Cloudlet> sortedFinishedCloudletList;
            sortedFinishedCloudletList = broker.getCloudletFinishedList();
            sortedFinishedCloudletList.sort(Comparator.comparingDouble(Cloudlet::getActualCpuTime));

            DecimalFormat df = new DecimalFormat("#.##");

            if(brokerLambdaEnergyConsumption.containsKey(((DatacenterBrokerLambda) broker).getLambdaValue())) {
                brokerLambdaEnergyConsumption.get(((DatacenterBrokerLambda) broker).getLambdaValue()).put(broker.getId(), Double.valueOf(df.format(TotalPowerConsumptionInKWatt).replaceAll(",", ".")));
            } else {
                Map<Long,Double> brokerEnergyConsumption = new TreeMap<>();
                brokerEnergyConsumption.put(broker.getId(), Double.valueOf(df.format(TotalPowerConsumptionInKWatt).replaceAll(",", ".")));
                brokerLambdaEnergyConsumption.put(((DatacenterBrokerLambda) broker).getLambdaValue(), brokerEnergyConsumption);
            }

            if(brokerLambdaEnergyConsumption.size() == 11){
                if(brokerLambdaEnergyConsumption.get(1.0).size() == NUMBER_OF_BROKERS) {
                    try(BufferedWriter br = new BufferedWriter(new FileWriter("outputEnergyPower.txt",true))) {
                        br.newLine();
                        br.write("---------------------------------------------------------------------------------------------------------\n");
                        br.newLine();
                        br.write("HAPS Stations Properties \n" +
                                "------------------------------------------\n" +
                                "Number of Stations: " + NUMBER_OF_HAPS +
                                ", Number of Hosts: " + HOST_HAPS_NUMBER +
                                ", Number of Vms: " + VMS_HAPS_NUMBER +
                                ", Mips for Host: " + mipsHAPSHost +
                                ", Ram for Host: " + ramHAPSHost +
                                ", Storage for Host: " + storageHAPSHost +
                                ", BW for Host: " + bwHAPSHost +
                                ", Mips for Vm: " + mipsHAPSVm +
                                ", Size for Vm: " + sizeHAPSVm +
                                ", Ram for Vm: " + ramHAPSVm +
                                ", BW for Vm: " + bwHAPSVm + "\n");
                        br.newLine();
                        br.write("Lambda Results \n" +
                                "------------------------------------------");
                        br.newLine();
                        for(Map.Entry entry : brokerLambdaEnergyConsumption.entrySet()) {
                            br.write("For Lambda: " + entry.getKey());
                            br.newLine();
                            for(Map.Entry value : ((Map<Long, Integer>)entry.getValue()).entrySet()) {
                                br.write("Broker ID: " + value.getKey() + ", Total Energy Consumption in KWatt: " + value.getValue());
                                br.newLine();
                                /*if(value.getKey(). == NUMBER_OF_BROKERS){
                                    br.newLine();
                                }*/
                            }
                        }
                        br.flush();
                        brokerLambdaEnergyConsumption.clear();
                    } catch (IOException e) {
                        System.out.println("Unable to read file ");
                    }
                }
            }
        }
    }

}
