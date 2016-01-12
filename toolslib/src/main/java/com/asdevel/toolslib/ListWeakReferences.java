package com.asdevel.toolslib;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by asdevel.com
 * Date: 04.06.2014
 * Time: 11:53
 */
public class ListWeakReferences <T>
{
    public interface Enumerator<T>
    {
        void enumerated(T t);
    }

    private LinkedHashSet<WeakReference<T>> m_lst = null;

    synchronized public void add(T t)
    {
        if (m_lst == null)
        {
            m_lst = new LinkedHashSet<>();
        }
        m_lst.add(new WeakReference<T>(t));
    }

    synchronized public void enumItems(Enumerator<T> en)
    {
        if (m_lst == null)
        {
            return;
        }
        HashSet<WeakReference<T>> lstToRemove = null;
        for (WeakReference<T> w : m_lst)
        {
            T t = w.get();
            if (t == null)
            {
                if (lstToRemove == null)
                {
                    lstToRemove = new HashSet<WeakReference<T>>();
                }
                lstToRemove.add(w);
                continue;
            }

            try
            {
                en.enumerated(t);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (lstToRemove != null) m_lst.remove(lstToRemove);

        if (m_lst.isEmpty())
        {
            m_lst = null;
        }
    }

    public boolean isEmpty()
    {
        return m_lst == null || m_lst.isEmpty();
    }

    public void clear()
    {
        if (m_lst != null) m_lst.clear();
    }

    public int size()
    {
        return m_lst != null ? m_lst.size() : 0;
    }
}
