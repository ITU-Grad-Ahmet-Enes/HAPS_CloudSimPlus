import plotly.graph_objects as go
import numpy as np

outputFile1 = open("smallHAPSOnlyNumbers_Cloudlet.txt", "r")
outputFile2 = open("bigHAPSOnlyNumbers_Cloudlet.txt", "r")

outputFile3 = open("smallHAPSOnlyNumbers_VmLifeTime.txt", "r")
outputFile4 = open("bigHAPSOnlyNumbers_VmLifeTime.txt", "r")

smallHAPS_Cloudlet = outputFile1.read().split('\n')
smallHAPS_Cloudlet.pop()

bigHAPS_Cloudlet = outputFile2.read().split('\n')
bigHAPS_Cloudlet.pop()

smallHAPS_Vm_Life_Time = outputFile3.read().split('\n')
smallHAPS_Vm_Life_Time.pop()

bigHAPS_Vm_Life_Time = outputFile4.read().split('\n')
bigHAPS_Vm_Life_Time.pop()

Number_of_Cloudlets = []
for i in range(len(smallHAPS_Cloudlet)):
    Number_of_Cloudlets.append((i + 1) * 25)
    smallHAPS_Cloudlet[i] = int(smallHAPS_Cloudlet[i])
    bigHAPS_Cloudlet[i] = int(bigHAPS_Cloudlet[i])

Delays = []
for i in range(len(smallHAPS_Vm_Life_Time)):
    Delays.append(25 + i * 100)
    smallHAPS_Vm_Life_Time[i] = int(smallHAPS_Vm_Life_Time[i])


fig = go.Figure()
fig.add_trace(go.Scatter(x=Number_of_Cloudlets, y=smallHAPS_Cloudlet, mode='lines+markers', name='SmallHAPS'))
fig.add_trace(go.Scatter(x=Number_of_Cloudlets, y=bigHAPS_Cloudlet, mode='lines+markers', name='BigHAPS'))

fig.update_layout(title_text="x:NumberOfCloudLets y:EnergyConsuptionInKw", width=1800, )
fig.show()

fig = go.Figure()
fig.add_trace(go.Scatter(x=Delays, y=smallHAPS_Vm_Life_Time, mode='lines+markers', name='SmallHAPS'))
fig.add_trace(go.Scatter(x=Delays, y=bigHAPS_Vm_Life_Time, mode='lines+markers', name='BigHAPS'))

fig.update_layout(title_text="x:MeanDelay y:EnergyConsuptionInKw", width=1800, )
fig.show()
