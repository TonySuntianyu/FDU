# Makefile for 路径规划系统

CXX = g++
CXXFLAGS = -std=c++17 -O2 -Wall
TARGET = path_planner
SOURCES = Graph.cpp Dijkstra.cpp FileParser.cpp TrafficCalculator.cpp main.cpp
OBJECTS = $(SOURCES:.cpp=.o)

# Windows系统
ifeq ($(OS),Windows_NT)
    TARGET = path_planner.exe
endif

all: $(TARGET)

$(TARGET): $(OBJECTS)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(OBJECTS)

%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

clean:
	rm -f $(OBJECTS) $(TARGET)

.PHONY: all clean

