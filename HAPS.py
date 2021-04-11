import plotly.graph_objects as go
import numpy as np
import pandas as pd
import plotly.express as px

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
smallHAPS_Cloudlet_Energy = []
smallHAPS_Cloudlet_Utilization = []
smallHAPS_Cloudlet_Mean_Total_Up_Time = []
type_small_cloudlet = []
type_big_cloudlet = []
bigHAPS_Cloudlet_Energy = []
bigHAPS_Cloudlet_Utilization = []
bigHAPS_Cloudlet_Mean_Total_Up_Time = []
for i in range(len(smallHAPS_Cloudlet)):
    if i < len(smallHAPS_Cloudlet) / 3:
        Number_of_Cloudlets.append(500 + i * 100)
        type_small_cloudlet.append('Small')
        type_big_cloudlet.append('Big')
    if i % 3 == 0:
        smallHAPS_Cloudlet_Energy.append(int(smallHAPS_Cloudlet[i]))
        bigHAPS_Cloudlet_Energy.append(int(bigHAPS_Cloudlet[i]))
    elif i % 3 == 1:
        smallHAPS_Cloudlet_Utilization.append(float(smallHAPS_Cloudlet[i].replace(',', '.')))
        bigHAPS_Cloudlet_Utilization.append(float(bigHAPS_Cloudlet[i].replace(',', '.')))
    elif i % 3 == 2:
        smallHAPS_Cloudlet_Mean_Total_Up_Time.append(int(smallHAPS_Cloudlet[i]))
        bigHAPS_Cloudlet_Mean_Total_Up_Time.append(int(bigHAPS_Cloudlet[i]))

Delays = []
smallHAPS_Vm_Life_Time_Energy = []
smallHAPS_Vm_Life_Time_Utilization = []
smallHAPS_Vm_Life_Time_Mean_Total_Up_Time = []
bigHAPS_Vm_Life_Time_Energy = []
bigHAPS_Vm_Life_Time_Utilization = []
bigHAPS_Vm_Life_Time_Mean_Total_Up_Time = []
type_small_vm = []
type_big_vm = []
for i in range(len(smallHAPS_Vm_Life_Time)):
    if i < len(smallHAPS_Vm_Life_Time) / 3:
        Delays.append(500 + i * 5000)
        type_small_vm.append('Small')
        type_big_vm.append('Big')
    if i % 3 == 0:
        smallHAPS_Vm_Life_Time_Energy.append(int(smallHAPS_Vm_Life_Time[i]))
        bigHAPS_Vm_Life_Time_Energy.append(int(bigHAPS_Vm_Life_Time[i]))
    elif i % 3 == 1:
        smallHAPS_Vm_Life_Time_Utilization.append(float(smallHAPS_Vm_Life_Time[i].replace(',', '.')))
        bigHAPS_Vm_Life_Time_Utilization.append(float(bigHAPS_Vm_Life_Time[i].replace(',', '.')))
    elif i % 3 == 2:
        smallHAPS_Vm_Life_Time_Mean_Total_Up_Time.append(int(smallHAPS_Vm_Life_Time[i]))
        bigHAPS_Vm_Life_Time_Mean_Total_Up_Time.append(int(bigHAPS_Vm_Life_Time[i]))
#
# fig = go.Figure()
# fig.add_trace(go.Scatter(x=Number_of_Cloudlets, y=smallHAPS_Cloudlet_Energy, mode='lines+markers', name='SmallHAPS'))
# fig.add_trace(go.Scatter(x=Number_of_Cloudlets, y=bigHAPS_Cloudlet_Energy, mode='lines+markers', name='BigHAPS'))
#
# fig.update_layout(title_text="x:NumberOfCloudLets y:EnergyConsuptionInKw", width=1800, )
# fig.show()
#
# fig = go.Figure()
# fig.add_trace(go.Scatter(x=Delays, y=smallHAPS_Vm_Life_Time_Energy, mode='lines+markers', name='SmallHAPS'))
# fig.add_trace(go.Scatter(x=Delays, y=bigHAPS_Vm_Life_Time_Energy, mode='lines+markers', name='BigHAPS'))
#
# fig.update_layout(title_text="x:MeanDelay y:EnergyConsuptionInKw", width=1800, )
# fig.show()


data1 = {'Cloudlet': Number_of_Cloudlets + Number_of_Cloudlets,
         'Energy': smallHAPS_Cloudlet_Energy + bigHAPS_Cloudlet_Energy,
         'MeanUtilization': smallHAPS_Cloudlet_Utilization + bigHAPS_Cloudlet_Utilization,
         'MeanUpTime': smallHAPS_Cloudlet_Mean_Total_Up_Time + bigHAPS_Cloudlet_Mean_Total_Up_Time,
         'Type': type_small_cloudlet + type_big_cloudlet}

data2 = {'Delays': Delays + Delays, 'Energy': smallHAPS_Vm_Life_Time_Energy + bigHAPS_Vm_Life_Time_Energy,
         'MeanUtilization': smallHAPS_Vm_Life_Time_Utilization + bigHAPS_Vm_Life_Time_Utilization,
         'MeanUpTime': smallHAPS_Vm_Life_Time_Mean_Total_Up_Time + bigHAPS_Vm_Life_Time_Mean_Total_Up_Time,
         'Type': type_small_vm + type_big_vm}

df1 = pd.DataFrame(data1)
df2 = pd.DataFrame(data2)

fig = px.scatter(
    data_frame=df1,
    x='Cloudlet',
    y='Energy',
    custom_data=['Energy', 'MeanUtilization', 'MeanUpTime'], color="Type"
)

fig.update_traces(mode="markers+lines",
                  hovertemplate="<br>".join([
                      "Cloudlet: %{x}",
                      "Energy: %{y}",
                      "MeanUtilization: %{customdata[1]}",
                      "MeanUpTime: %{customdata[2]}",
                  ])
                  )
fig.show()

# #####################################################################################

fig2 = px.scatter(
    data_frame=df2,
    x='Delays',
    y='Energy',
    custom_data=['Energy', 'MeanUtilization', 'MeanUpTime'], color="Type"
)

fig2.update_traces(mode="markers+lines",
                   hovertemplate="<br>".join([
                       "Delays: %{x}",
                       "Energy: %{y}",
                       "MeanUtilization: %{customdata[1]}",
                       "MeanUpTime: %{customdata[2]}",
                   ])
                   )
fig2.show()
