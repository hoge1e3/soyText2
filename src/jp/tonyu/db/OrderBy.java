package jp.tonyu.db;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class OrderBy implements List<OrderByElem> {
    List<OrderByElem> ords=new Vector<OrderByElem>();

    /**
     *
     * @param spec for example, "name,-lastupdate"
     * @return
     */
    public static OrderBy parse(String spec) {
        String[] s=spec.split(",");
        OrderBy res=new OrderBy();
        for (String ss: s) {
            OrderByElem o=OrderByElem.parse(ss);
            res.add(o);
        }
        return res;
    }
    @Override
    public String toString() {
        String com="";
        StringBuilder b=new StringBuilder();
        for (OrderByElem o:ords) {
            b.append(com);
            com=",";
            b.append(o);
        }
        return b.toString();
    }
    public String indexName(String tableName) {
        StringBuilder b=new StringBuilder(tableName);
        for (OrderByElem o:ords) {
            b.append("__");
            b.append(o.name);
            if (o.isDescend) b.append("_d");
        }
        return b.toString().replaceAll("\\s", "").replaceAll("\\W", "_");
    }
    //------------
    public void add(int arg0, OrderByElem arg1) {
        ords.add(arg0, arg1);
    }
    public boolean add(OrderByElem arg0) {
        return ords.add(arg0);
    }
    public boolean addAll(Collection<? extends OrderByElem> arg0) {
        return ords.addAll(arg0);
    }
    public boolean addAll(int arg0, Collection<? extends OrderByElem> arg1) {
        return ords.addAll(arg0, arg1);
    }
    public void clear() {
        ords.clear();
    }
    public boolean contains(Object arg0) {
        return ords.contains(arg0);
    }
    public boolean containsAll(Collection<?> arg0) {
        return ords.containsAll(arg0);
    }
    public boolean equals(Object arg0) {
        return ords.equals(arg0);
    }
    public OrderByElem get(int arg0) {
        return ords.get(arg0);
    }
    public int hashCode() {
        return ords.hashCode();
    }
    public int indexOf(Object arg0) {
        return ords.indexOf(arg0);
    }
    public boolean isEmpty() {
        return ords.isEmpty();
    }
    public Iterator<OrderByElem> iterator() {
        return ords.iterator();
    }
    public int lastIndexOf(Object arg0) {
        return ords.lastIndexOf(arg0);
    }
    public ListIterator<OrderByElem> listIterator() {
        return ords.listIterator();
    }
    public ListIterator<OrderByElem> listIterator(int arg0) {
        return ords.listIterator(arg0);
    }
    public OrderByElem remove(int arg0) {
        return ords.remove(arg0);
    }
    public boolean remove(Object arg0) {
        return ords.remove(arg0);
    }
    public boolean removeAll(Collection<?> arg0) {
        return ords.removeAll(arg0);
    }
    public boolean retainAll(Collection<?> arg0) {
        return ords.retainAll(arg0);
    }
    public OrderByElem set(int arg0, OrderByElem arg1) {
        return ords.set(arg0, arg1);
    }
    public int size() {
        return ords.size();
    }
    public List<OrderByElem> subList(int arg0, int arg1) {
        return ords.subList(arg0, arg1);
    }
    public Object[] toArray() {
        return ords.toArray();
    }
    public <T> T[] toArray(T[] arg0) {
        return ords.toArray(arg0);
    }
    public OrderBy add(String name, boolean isDescend) {
        add(new OrderByElem(name, isDescend));
        return this;
    }
    public OrderBy desc(String name) {
        return add(name,true);
    }
    public OrderBy asc(String name) {
        return add(name,false);
    }


}
