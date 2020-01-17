#!/usr/bin/env bash

java -cp ${PWD}/src launchFileWriter


CURRDIR=${PWD}
{
gnome-terminal --execute roslaunch ${PWD}/launch/turtlebot_setup.launch
gnome-terminal --execute roslaunch ${PWD}/launch/user_world.launch
} &> /dev/null
