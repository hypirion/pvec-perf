## Estimate cache lines required for a persistent vector implementation with a
## tail by these formulas.

from math import log, ceil

bbits = 5 # number of bits for the branching factor (5 -> 32-way branching)
b = 1 << bbits # the branching factor
o = 40 # overhead per node - 40 bytes is the amount for Clojure's implementation
p = 4  # Size of a pointer (4 with compressedOops on the JVM)
M = 64 # bytes in a single cache line (usually 64)

## Number of cache lines a node occupies at minimum.
nodeLines = int(ceil((o+p*(b+2))/float(M)))

## This function calculates size of the tail. Useful to avoid peeking into the
## tail array itself, which may incur another lookup.
def tailSize(n):
    if n == 0:
        return 0
    else:
        return ((n-1) & (b-1))+1

## The first index within the persistent vector tail. Is also the size of the
## vector trie.
def tailOffset(n):
    return (n-1) & (~(b-1))

## Don't use this one for general-purpose shift calculations. Only verified to
## work properly for n % b == 0, and n >= b. Prefer to store the shift in the
## vector head if you're going to implement one.
def shift(n):
    return int(ceil(log(n,b))-1)*bbits

## number of different cache lines required to be looked up for an append.
## Doesn't calculate cache lines written, which is also important to realise.

## Assumes (naively) optimal layout without overlap: That is, this formula
## ignores that a node which is e.g. 2.6 cache lines long, can theoretically be
## spread over 4 actual cache lines. Additionally, it assumes that two nodes
## cannot share cache line: Practically, two nodes that are 2.4 cache lines long
## may reside in only 5 cache lines. This algorithm assumes they are stored in
## 6. Finally, it also assumes that the Node class pointing to the object[]
## array is stored directly in front of the array itself – which is a bit of a
## stretch.
def cacheReadsForAppend(n):
    if n % b != 0:
        return 1 + int(ceil((o+p*tailSize(n+1))/float(M)))
    if n == b or log(n-b,b).is_integer():
        return 1
    diverges = tailOffset(n) ^ (tailOffset(n)-1)
    level = shift(n)
    count = 1
    while level > 0:
        count += nodeLines
        if diverges >> level != 0:
            return count
        level -= bbits

## Cache lines required for memory allocations for append. JVMs has to zero
## memory, so I assume this is done in this step as well: This means the nodes
## actually have to be stored in this step.
def mallocsForAppend(n):
    tailLines = int(ceil((o+p*tailSize(n+1))/float(M)))
    if n % b != 0 or n == b:
        return 1 + tailLines
    else:
        return 1 + nodeLines * int(ceil(log(n,b))-1) + tailLines

## also: lookups, pops, updates... long list of algos. Lookups and updates are
## rather straightforward, whereas pops should be close to the inverse of a
## push.
