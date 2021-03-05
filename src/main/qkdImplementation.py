# -*- coding: utf-8 -*-
"""
Created on Wed Feb 10 16:14:41 2021

@author: Marc
"""
from numpy.random import *
from qiskit import QuantumCircuit, execute, Aer, IBMQ
from qiskit.compiler import transpile, assemble
import sys

"""So 1 represents either |1> or |->, 0 represents |0> or |+> for bits
For the bases, 0 represents Z basis and 1 represents X basis"""
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
        self.matchingIndices = []#indicies where sender and this measured in the same axis
        self.__privateKeyData = []
        self.compareKeyData = []
        
    def makeSendCircuits(keyData):
        circuits = []
        for i in range(len(keyData.sendBits)):
            circuit = QuantumCircuit(1,2)
            if (keyData.sendBits[i] == 1):
                circuit.initialize([0,1], 0)
            if (keyData.sendBases[i] == 1):
                circuit.h(0)
            circuits.append(circuit)
        return circuits

    def addMatchingIndex(self, i):
        self.matchingIndices.append(i)

    def makeKey(self):
        numMatching = len(self.matchingIndices)
        for i in range(numMatching):
            self.__privateKeyData.append(self.sendBits[self.matchingIndices[i]])
        self.compareKeyData = self.__privateKeyData[:int(numMatching/2)]

    def aa(self):
        return self.__privateKeyData



class Eavesdropper:
    def __randbits__(num):
        gen = default_rng()
        return gen.integers(0,1,num, endpoint=True)
    
    def __init__(self, num, bases = []):
        if bases == []:
            bases = Eve.__randbits__(num)
        self.num = num            
        self.receiveBases = bases
        self.receivedBits = []
    
    def makeReceiveCircuits(self):
        recCircuits = []
        for i in range(self.num):
            recCircuit = QuantumCircuit(1,2)
            if (self.receiveBases[i] == 1):
                recCircuit.h(0)
            recCircuit.measure(0,1)
            recCircuits.append(recCircuit)
        return recCircuits
    
    def addBit(self, bit):
        self.receivedBits.append(bit)
        
    def printMeasured(self):
        for i in self.receivedBits:
            print(i, end='')
        print('')
        



class KeyReceiver:
    def __randbits__(num):
        gen = default_rng()
        return gen.integers(0,1,num, endpoint=True)

    def __init__(self, num, bases = []):
        if bases == []:
            bases = KeyReceiver.__randbits__(num)
        
        self.num = num
        self.receivedBits = []
        self.receiveBases = bases
        self.matchingIndices = []
        self.__privateKeyData = []
        self.compareKeyData = []

    def makeReceiveCircuits(self):
        recCircuits = []
        for i in range(self.num):
            recCircuit = QuantumCircuit(1,2)
            if (self.receiveBases[i] == 1):
                recCircuit.h(0)
            recCircuit.measure(0,0)
            recCircuits.append(recCircuit)
        return recCircuits

    def addBit(self, bit):
        self.receivedBits.append(bit)

    def addMatchingIndex(self, i):
        self.matchingIndices.append(i)

    def makeKey(self):
        numMatching = len(self.matchingIndices)
        for i in range(numMatching):
            self.__privateKeyData.append(self.receivedBits[self.matchingIndices[i]])
        self.compareKeyData = self.__privateKeyData[:int(numMatching/2)]
    def aa(self):
        return self.__privateKeyData
    def printMeasured(self):
        for i in self.receivedBits:
            print(i, end='')
        print('')
        
def ReceiveData(sender, receiver, eve=None, backend = Aer.get_backend('qasm_simulator')):
    if eve is None:
        ReceiveDataNormal(sender, receiver, backend)
    else:
        ReceiveDataEavesdropper(sender, receiver, eve, backend)

def ReceiveDataEavesdropper(sender, receiver, eve, backend):
    #Send the bits across the channel
    sendCircuits = sender.makeSendCircuits()
    eveCircuits = eve.makeReceiveCircuits()
    recCircuits = receiver.makeReceiveCircuits()
    for i in range(sender.num):
        sendAndReceive = sendCircuits[i] + eveCircuits[i] + recCircuits[i]
        result = execute(sendAndReceive, backend, shots=1, memory=True).result()
        receiver_bit = int(result.get_memory()[0][1:])
        receiver.addBit(receiver_bit)
        eve_bit = int(result.get_memory()[0][0:1])
        eve.addBit(eve_bit)
    #Now compare and see where the sender and receiver measured in the same base
    for i in range(sender.num):
        if sender.sendBases[i] == receiver.receiveBases[i]:
            receiver.addMatchingIndex(i)
            sender.addMatchingIndex(i)

def ReceiveDataNormal(sender, receiver, backend):
    #Send the bits across the channel
    sendCircuits = sender.makeSendCircuits()
    recCircuits = receiver.makeReceiveCircuits()
    for i in range(sender.num):
        sendAndReceive = sendCircuits[i] + recCircuits[i]
        result = execute(sendAndReceive, backend, shots=1, memory=True).result()
        measured_bit = int(result.get_memory()[0])
        receiver.addBit(measured_bit)
    #Now compare and see where the sender and receiver measured in the same base
    for i in range(sender.num):
        if sender.sendBases[i] == receiver.receiveBases[i]:
            receiver.addMatchingIndex(i)
            sender.addMatchingIndex(i)

"""Turns a string containing 1s and 0s into a list of 1s and 0s"""
def listFromString(str):
    list = []
    for i in str:
        list.append(int(i))
    return list


def main():    
    keyLength = len(sys.argv[1])
    aliceBits = listFromString(sys.argv[1])
    aliceBases = listFromString(sys.argv[2])
    bobBases = listFromString(sys.argv[3])
    Alice = KeySender(keyLength, aliceBits, aliceBases)
    Bob = KeyReceiver(keyLength, bobBases)
    Eve = None
    
    if len(sys.argv) > 4:
        Eve = Eavesdropper(keyLength, listFromString(sys.argv[4]))
    
    ReceiveData(Alice, Bob, Eve)
    Bob.printMeasured()
    if Eve is not None:
        Eve.printMeasured()
    #Alice.makeKey()
    #Bob.makeKey()
    #print(Bob.aa())
    #print(Alice.aa())
main()












