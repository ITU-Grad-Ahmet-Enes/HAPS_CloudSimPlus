package org.cloudsimplus.haps.headers;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerAbstract;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.vms.Vm;

public class BigSmallDCBroker extends DatacenterBrokerAbstract {
    private double lambdaValue;

    public double getLambdaValue() {
        return lambdaValue;
    }

    public void setLambdaValue(double lambdaValue) {
        this.lambdaValue = lambdaValue;
    }

    private int numberOfCloudletPerBroker;
    private int vmPerCloudlet;
    private boolean singleVM;
    private String typeHAPS;

    /**
     * Creates a new DatacenterBroker.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     */
    public BigSmallDCBroker(final CloudSim simulation) {
        this(simulation, "");
    }

    /**
     * Creates a DatacenterBroker giving a specific name.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     * @param name the DatacenterBroker name
     */
    public BigSmallDCBroker(final CloudSim simulation, final String name) {
        super(simulation, name);
    }

    /**
     * Creates a DatacenterBroker giving a specific name.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     * @param name the DatacenterBroker name

     */
    public BigSmallDCBroker(final CloudSim simulation, final String name, int numberOfCloudletPerBroker, int vmPerCloudlet) {
        super(simulation, name);
        this.numberOfCloudletPerBroker = numberOfCloudletPerBroker;
        this.vmPerCloudlet = vmPerCloudlet;
    }

    public BigSmallDCBroker(final CloudSim simulation, final String name, int numberOfCloudletPerBroker, String typeHAPS) {
        super(simulation, name);
        this.numberOfCloudletPerBroker = numberOfCloudletPerBroker;
        this.typeHAPS = typeHAPS;
    }

    public BigSmallDCBroker(final CloudSim simulation, final String name, int numberOfCloudletPerBroker) {
        super(simulation, name);
        this.numberOfCloudletPerBroker = numberOfCloudletPerBroker;
        this.singleVM = true;
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>It applies a Round-Robin policy to cyclically select
     * the next Datacenter from the list. However, it just moves
     * to the next Datacenter when the previous one was not able to create
     * all {@link #getVmWaitingList() waiting VMs}.</p>
     *
     * <p>This policy is just used if the selection of the closest Datacenter is not enabled.
     * Otherwise, the {@link #closestDatacenterMapper(Datacenter, Vm)} is used instead.</p>
     *
     * @param lastDatacenter {@inheritDoc}
     * @param vm {@inheritDoc}
     * @return {@inheritDoc}
     * @see #setSelectClosestDatacenter(boolean)
     */
    @Override
    protected Datacenter defaultDatacenterMapper(final Datacenter lastDatacenter, final Vm vm) {
        if(getDatacenterList().isEmpty()) {
            throw new IllegalStateException("You don't have any Datacenter created.");
        }
        if (singleVM) {
            return getDatacenterList().get((int) vm.getId());
        }
        if(typeHAPS.equals("b")){
            return getDatacenterList().get((0));
        }
        else if(typeHAPS.equals("s")){
            return getDatacenterList().get((int) vm.getId() % 10);
        }
        return getDatacenterList().get((int) vm.getId() / (numberOfCloudletPerBroker/vmPerCloudlet));
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>It applies a Round-Robin policy to cyclically select
     * the next Vm from the {@link #getVmWaitingList() list of waiting VMs}.</p>
     *
     * @param cloudlet {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Vm defaultVmMapper(final Cloudlet cloudlet) {

        if (cloudlet.isBoundToVm()) {
            return cloudlet.getVm();
        }

        if (getVmExecList().isEmpty()) {
            return Vm.NULL;
        }

        if(singleVM){
            int division = (int) (cloudlet.getId() / numberOfCloudletPerBroker);
            return getDatacenterList().get(division).getHost(0).getVmList().get(0);
        }
        if(typeHAPS.equals("b")){
            int division = (int) (cloudlet.getId() / numberOfCloudletPerBroker);
            return getDatacenterList().get(division).getHost(0).getVmList().get((int) cloudlet.getId() % 100);
        }
        else if(typeHAPS.equals("s")){
            int division = (int) (cloudlet.getId() / numberOfCloudletPerBroker);
            return getDatacenterList().get(division).getHost(0).getVmList().get((int) cloudlet.getId() % 10);
        }
        int division = (int) (cloudlet.getId() / numberOfCloudletPerBroker);
        return getDatacenterList().get(division).getHost(0).getVmList().get((int) cloudlet.getId() % (numberOfCloudletPerBroker/vmPerCloudlet));
    }
}
