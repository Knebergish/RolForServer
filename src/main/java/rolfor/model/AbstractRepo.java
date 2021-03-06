package rolfor.model;


import rolfor.HibernateUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


@SuppressWarnings("WeakerAccess")
public abstract class AbstractRepo<T extends Entity, M extends T, R extends M> implements Repo<T, M> {
	protected final EntityManager   em = HibernateUtil.getSessionFactory().createEntityManager();
	protected final CriteriaBuilder cb = em.getCriteriaBuilder();
	
	@Override
	public R find(Integer id) {
		return em.find(getEntityClass(), id);
	}
	
	@Override
	public M findMutable(Integer id) {
		return find(id);
	}
	
	@Override
	public void remove(T item) {
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		em.remove(item);
		em.flush();
		transaction.commit();
	}
	
	@Override
	public T add(T item) {
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		em.persist(item);
		em.flush();
		transaction.commit();
		return item;
	}
	
	@Override
	public T save(Integer id, T item) {
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		R entity = em.find(getEntityClass(), id);
		copy(item, entity);
		em.persist(entity);
		em.flush();
		transaction.commit();
		return entity;
	}
	
	@Override
	public CriteriaQuery<R> getAllQuery() {
		CriteriaQuery<R> cq        = cb.createQuery(getEntityClass());
		Root<R>          rootEntry = cq.from(getEntityClass());
		return cq.select(rootEntry).orderBy(cb.asc(rootEntry.get("id")));
	}
	
	@Override
	public <E> TypedQuery<E> buildQuery(CriteriaQuery<E> query) {
		return em.createQuery(query);
	}
	
	
	@Override
	public CriteriaQuery<R> getSelectQuery() {
		CriteriaQuery<R> cq        = cb.createQuery(getEntityClass());
		Root<R>          rootEntry = cq.from(getEntityClass());
		return cq.select(rootEntry);
	}
	
	@Override
	public TypedQuery<? extends T> getPagedQuery(CriteriaQuery<? extends T> query, int pageNumber, int pageSize) {
		TypedQuery<? extends T> typedQuery = em.createQuery(query);
		typedQuery.setFirstResult((pageNumber - 1) * pageSize);
		typedQuery.setMaxResults(pageSize);
		return typedQuery;
	}
	
	@Override
	public Long getPagesCount(CriteriaQuery<? extends T> query, int pageSize) {
		em.createQuery(query); // I hate this shit
		
		final CriteriaQuery<Long>    countQuery = cb.createQuery(Long.class);
		final Root<? extends Entity> countRoot  = countQuery.from(query.getResultType());
		
		countQuery.select(cb.count(countRoot));
		if (query.getRestriction() != null) {
			countQuery.where(query.getRestriction());
		}
		countRoot.alias(query.getRoots().iterator().next().getAlias());
		
		CriteriaQuery<Long> resultQuery = countQuery.distinct(query.isDistinct());
		Long                rowsCount   = em.createQuery(resultQuery).getSingleResult();
		return rowsCount / pageSize + (rowsCount % pageSize > 0 ? 1 : 0);
	}
	
	
	@Override
	public abstract Class<R> getEntityClass();
	
	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return cb;
	}
	
	protected abstract void copy(T from, M to);
}
