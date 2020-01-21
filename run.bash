#!/usr/bin/env bash

var="$(java -classpath src launchFileWriter)"
COUNTER=-1
BASE=-1 # do this so I can read poses starting a 0
TBAMT=0
declare -a INIT_POSES

read_var() {
    for line in $var; do
        if [ "$COUNTER" -eq "$BASE" ]; then
            TBAMT=$line
        else
            INIT_POSES[$COUNTER]=$line
        fi
        COUNTER=$(($COUNTER + 1))
    done
}

#turtlebot setup then launches the world
turtlebot_startup() {
    gnome-terminal --execute roslaunch launch/turtlebot_setup.launch
    sleep 5
    gnome-terminal --execute roslaunch launch/user_world.launch

} &>/dev/null

#map launch
launch_map_server() {
    #super finicky, but INIT_POSES["final_index"] stores the occupancy grid directory.
    occ_grid_index=$((3 * $TBAMT))
    OCCUPANCY_GRID_DIR=${INIT_POSES[$occ_grid_index]}
    #running map server
    sleep 5
    gnome-terminal --execute rosrun map_server map_server $OCCUPANCY_GRID_DIR
}

#launches the nav files
launch_nav() {
    COUNTER=0
    i=0
    while [ $i -lt $TBAMT ]; do
        sleep 5
        gnome-terminal --execute roslaunch launch/navigation.launch ns:=tb3_$i initial_pose_x:=${INIT_POSES[$COUNTER]} initial_pose_y:=${INIT_POSES[$COUNTER + 1]} initial_pose_a:=${INIT_POSES[$COUNTER + 2]}
        COUNTER=$(($COUNTER + 3))
        i=$(($i + 1))
    done
}

launch_rviz() {
   gnome-terminal --execute rosrun rviz rviz -d rviz/multi_nav_tb.rviz 
}

read_var
turtlebot_startup
launch_map_server
launch_nav
launch_rviz
