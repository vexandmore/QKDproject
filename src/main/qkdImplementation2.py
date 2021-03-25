# -*- coding: utf-8 -*-
"""
This script provides the Alice with her circuits (based on the number of bits
, the bits, and the bases.)
Bits and bases are passed to stdin as space-separated bistrings.
Circuits are returned as json-wrapped qasm.

And also acts as a way to measure the circuits. (pass in bases as bitstring,
followed by a space, followed by json consisting in a list of qasm strings.)
@author: Marc
"""

from numpy.random import *
import json
from qiskit import QuantumCircuit, execute, Aer, IBMQ
from qiskit.compiler import transpile, assemble
import sys
import time


def measureMessage(sendCircuits, bases, backend):
        measurements = []
        for i in range(len(bases)):
            if (bases[i] == 1):
                sendCircuits[i].h(0)
            sendCircuits[i].measure(0,0)
            result = execute(sendCircuits[i], backend, shots=1, memory=True).result()
            receiver_bit = int(result.get_memory()[0])
            measurements.append(receiver_bit)
        return measurements


def listFromString(str):
    list = []
    for i in str:
        list.append(int(i))
    return list


class KeySender:
    def __randbits__(num):
        gen = default_rng()
        return gen.integers(0,1,num, endpoint=True)

    def __init__ (self, num, bits=[], bases=[]):
        if bits == []:
            bits = KeySender.__randbits__(num)
        if bases == []:
            bases = KeySender.__randbits__(num)
        
        self.num = num
        self.sendBits = bits
        self.sendBases = bases
        
    def makeSendCircuits(keyData):
        circuits = []
        for i in range(len(keyData.sendBits)):
            circuit = QuantumCircuit(1,1)
            if (keyData.sendBits[i] == 1):
                circuit.x(0)
            if (keyData.sendBases[i] == 1):
                circuit.h(0)
            circuits.append(circuit)
        return circuits



def main():
    backend = Aer.get_backend('qasm_simulator')
    while True:
        lineIn = input()
        #send Alice her circuits
        if lineIn[-1] == '0' or lineIn[-1] == '1':
            inArgs = lineIn.split()
            numBits = int(inArgs[0])
            bits = listFromString(inArgs[1])
            bases = listFromString(inArgs[2])
            
            alice = KeySender(numBits, bits, bases)
            circuits = alice.makeSendCircuits()
            qasmCircuits = []
            for i in circuits:
                qasmCircuits.append(i.qasm())
            print(json.dumps(qasmCircuits))
        
        #measure circuits for Bob (or Eve)
        else :
            startT = time.time()#
            inArgs = lineIn.split(' ', 1)
            bases = listFromString(inArgs[0])
            qasmCircuits = json.loads(inArgs[1])
            jsonT = time.time()#
            circuits = []
            for i in qasmCircuits:
                circuits.append(QuantumCircuit.from_qasm_str(i))
            constructT = time.time()#
            measurements = measureMessage(circuits, bases, backend)
            measureT = time.time()
            
            #provide results
            for i in measurements:
                print(i, end='')
            print (' ', end='')#Space
            #provide new circuits
            for i in circuits:
                print(json.dumps(i.qasm()), end=' ')
            print('')#final newline
            
            #print time
            print('json time', end='')
            print(jsonT - startT)
            print('ctor time ', end='')
            print(constructT - startT)
            print('measure time', end='')
            print(measureT - startT)
main()