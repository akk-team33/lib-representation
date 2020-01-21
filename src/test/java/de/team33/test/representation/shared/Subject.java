package de.team33.test.representation.shared;

import java.util.*;

import static java.util.Collections.*;

public class Subject {

    private int thePrimitive;
    private String theString;
    private Number theNumber;
    private byte[] theByteArray = {};
    private Object theObject;
    private List<Object> theList;
    private Set<Object> theSet;
    private Map<Object, Object> theMap;

    public final int getThePrimitive() {
        return thePrimitive;
    }

    public final Subject setThePrimitive(final int thePrimitive) {
        this.thePrimitive = thePrimitive;
        return this;
    }

    public final String getTheString() {
        return theString;
    }

    public final Subject setTheString(final String theString) {
        this.theString = theString;
        return this;
    }

    public final Number getTheNumber() {
        return theNumber;
    }

    public final Subject setTheNumber(final Number theNumber) {
        this.theNumber = theNumber;
        return this;
    }

    public final byte[] getTheByteArray() {
        return theByteArray.clone();
    }

    public final Subject setTheByteArray(final byte[] theByteArray) {
        this.theByteArray = theByteArray.clone();
        return this;
    }

    public final Object getTheObject() {
        return theObject;
    }

    public final Subject setTheObject(final Object theObject) {
        this.theObject = theObject;
        return this;
    }

    public final List<Object> getTheList() {
        //noinspection ReturnOfNull
        return (null == theList) ? null : unmodifiableList(theList);
    }

    public final Subject setTheList(final Collection<?> theList) {
        //noinspection AssignmentToNull
        this.theList = (null == theList) ? null : new ArrayList<>(theList);
        return this;
    }

    public final Set<Object> getTheSet() {
        //noinspection ReturnOfNull
        return (null == theSet) ? null : unmodifiableSet(theSet);
    }

    public final Subject setTheSet(final Collection<?> theSet) {
        //noinspection AssignmentToNull
        this.theSet = (null == theSet) ? null : new HashSet<>(theSet);
        return this;
    }

    public final Map<Object, Object> getTheMap() {
        //noinspection ReturnOfNull
        return (null == theMap) ? null : unmodifiableMap(theMap);
    }

    public final Subject setTheMap(final Map<?, ?> theMap) {
        //noinspection AssignmentToNull
        this.theMap = (null == theMap) ? null : new HashMap<>(theMap);
        return this;
    }
}
