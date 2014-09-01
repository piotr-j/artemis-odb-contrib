package com.artemis;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.artemis.annotations.Wire;
import com.artemis.managers.UuidEntityManager;

/**
 * Tracks unique entity configurations, meant to make life easier when developing. Will
 * later be adapted to assist in the creation of specialized entity factories.
 * <p>
 * If {@link UuidEntityManager} is added to the world, relationships between different
 * entity configurations are tracked as well.
 * </p>
 * <p>
 * This manager prints what it has collected upon calling {@link World#dispose()}
 * </p>
 */
@Wire(failOnNull=false)
public class EntityCompositionTracker extends Manager {
    
    private BitSet bits = new BitSet();
    private Set<BitSet> observed = new HashSet<BitSet>();
    private Map<UUID, Set<BitSet>> uuidToComponents = new HashMap<UUID, Set<BitSet>>();
    
    private UuidEntityManager uuid;
    
    @Override
    protected void initialize() {
    }
    
    @Override
    public void added(Entity e) {
        process(e);
    }
    
    @Override
    public void changed(Entity e) {
        process(e);
    }
    
    private void process(Entity e) {
        bits.clear();
        bits.or(e.getComponentBits());
        if (!observed.contains(bits))
            observed.add((BitSet)bits.clone());
        
        if (uuid != null) {
            processUuid(e, bits);
        }
    }
    
    private void processUuid(Entity e, BitSet componentBits) {
        UUID uuid = e.getUuid();
        Set<BitSet> entityStates = uuidToComponents.get(uuid);
        if (entityStates == null) {
            entityStates = new HashSet<BitSet>();
            uuidToComponents.put(uuid, entityStates);
        }
        
        if (!entityStates.contains(componentBits))
            entityStates.add((BitSet)componentBits.clone());
    }
    
    public List<Composition> getCompositions() {
        ComponentTypeFactory typeFactory = world.getComponentManager().typeFactory;
        List<Composition> compositions = new ArrayList<Composition>();
        
        for (BitSet bs : observed) {
            Composition composition = new Composition();
            composition.componentBits.or(bs);
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                composition.components.add(typeFactory.getTypeFor(bs.nextSetBit(i)));
            }
            compositions.add(composition);
        }
        
        return compositions;
    }
    
    @Override
    protected void dispose() {
        if (uuid == null) {
            printCompositions();
        } else {
            printLinkedCompositions();
        }
    }

    public void printCompositions() {
        List<Composition> compositions = getCompositions();
        
        System.out.println("Unique entity configurations.");
        
        for (int i = 0, s = compositions.size(); s > i; i++) {
            System.out.println("\n#" + (i + 1) + ":");
            Composition composition = compositions.get(i);
            print(composition);
        }
    }

    private static void print(Composition composition) {
        for (ComponentType ct : composition.components) {
            System.out.println("\t" + ct.getType().getName());
        }
    }
    
    public void printLinkedCompositions() {
        Set<Set<BitSet>> variations = new HashSet<Set<BitSet>>();
        for (Set<BitSet> variation : uuidToComponents.values()) {
            variations.add(variation);
        }
        
        Map<BitSet,Composition> compositionMap = getCompositionMap();
        
        System.out.println("\nUnique entity states: " + variations.size());
        int group = 0;
        for (Set<BitSet> bitsets : variations) {
            System.out.println("Group #" + (++group));
            
            int configuration = 0;
            for (BitSet bs : bitsets) {
                System.out.println("Entity Configuration #" + (++configuration) + ":");
                print(compositionMap.get(bs));
                System.out.println();
            }
        }
    }

    private Map<BitSet, Composition> getCompositionMap() {
        Map<BitSet, Composition> compositionMap = new HashMap<BitSet,Composition>();
        for (Composition c : getCompositions()) {
            compositionMap.put(c.componentBits, c);
        }
        return compositionMap;
    }

    public static class Composition {
        public BitSet componentBits = new BitSet();
        public Set<ComponentType> components = new TreeSet<ComponentType>(new Comparator<ComponentType>() {
            @Override
            public int compare(ComponentType o1, ComponentType o2) {
                return o1.getType().getSimpleName().compareTo(o2.getType().getSimpleName());
            }
        });
        

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((components == null) ? 0 : components.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Composition other = (Composition)obj;
            if (components == null) {
                if (other.components != null) return false;
            }
            else if (!components.equals(other.components)) return false;
            return true;
        }
    }
}
