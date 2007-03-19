package org.jboss.seam.wiki.core.dao;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.wiki.core.model.Node;
import org.jboss.seam.wiki.core.model.Directory;
import org.jboss.seam.wiki.core.model.Document;
import org.jboss.seam.wiki.core.model.File;
import org.jboss.seam.Component;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.util.List;
import java.util.Date;

/**
 * DAO for nodes, transparently respects security access levels.
 * <p>
 * All node access should go through this component, this component knows
 * about access levels because it relies on a restricted (filtered) Entitymanager.
 *
 * @author Christian Bauer
 *
 */
@Name("nodeDAO")
@AutoCreate
@Transactional
public class NodeDAO {

    // Most of the DAO methods use this
    @In protected EntityManager restrictedEntityManager;

    // Some run unrestricted (e.g. internal unique key validation of wiki names)
    // Make sure that these methods do not return detached objects!
    @In protected EntityManager entityManager;


    public void makePersistent(Node node) {
        entityManager.joinTransaction();
        entityManager.persist(node);
    }

    public Node findNode(Long nodeId) {
        restrictedEntityManager.joinTransaction();
        try {
            return (Node) restrictedEntityManager
                    .createQuery("select n from Node n where n.id = :nodeId")
                    .setParameter("nodeId", nodeId)
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    public Node findNodeInArea(Long areaNumber, String wikiname) {
        return findNodeInArea(areaNumber, wikiname, restrictedEntityManager);
    }

    private Node findNodeInArea(Long areaNumber, String wikiname, EntityManager em) {
        em.joinTransaction();

        try {
            return (Node) em
                    .createQuery("select n from Node n where n.areaNumber = :areaNumber and n.wikiname = :wikiname")
                    .setParameter("areaNumber", areaNumber)
                    .setParameter("wikiname", wikiname)
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    public Document findDocumentInArea(Long areaNumber, String wikiname) {
        restrictedEntityManager.joinTransaction();

        try {
            return (Document) restrictedEntityManager
                    .createQuery("select d from Document d where d.areaNumber = :areaNumber and d.wikiname = :wikiname")
                    .setParameter("areaNumber", areaNumber)
                    .setParameter("wikiname", wikiname)
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    public Directory findDirectoryInArea(Long areaNumber, String wikiname) {
        restrictedEntityManager.joinTransaction();

        try {
            return (Directory) restrictedEntityManager
                    .createQuery("select d from Directory d where d.areaNumber = :areaNumber and d.wikiname = :wikiname")
                    .setParameter("areaNumber", areaNumber)
                    .setParameter("wikiname", wikiname)
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    public Directory findArea(String wikiname) {
        restrictedEntityManager.joinTransaction();

        try {
            return (Directory) restrictedEntityManager
                    .createQuery("select d from Directory d where d.parent = :root and d.wikiname = :wikiname")
                    .setParameter("root", Component.getInstance("wikiRoot"))
                    .setParameter("wikiname", wikiname)
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    public List<Document> findDocumentsOrderByLastModified(int maxResults) {
        return (List<Document>)restrictedEntityManager
                .createQuery("select d from Document d order by d.lastModifiedOn desc")
                .setMaxResults(maxResults)
                .getResultList();
    }

    public Node findHistoricalNode(Long historyId) {
        Node historicalNode = (Node)getSession().get("HistoricalDocument", historyId);
        getSession().evict(historicalNode);
        return historicalNode;
    }

    public void persistHistoricalNode(Node historicalNode) {
        // TODO: Ugh, concatenating class names to get the entity name?!
        getSession().persist("Historical"+historicalNode.getClass().getSimpleName(), historicalNode);
        getSession().flush();
        getSession().evict(historicalNode);
    }

    public int removeHistoricalNodes(Node node) {
        if (node == null) return 0;
        return getSession().createQuery("delete from HistoricalNode n where n.nodeId = :nodeId")
                            .setParameter("nodeId", node.getId())
                            .executeUpdate();
    }

    @SuppressWarnings({"unchecked"})
    public List<Node> findHistoricalNodes(Node node) {
        if (node == null) return null;
        return getSession().createQuery("select n from HistoricalNode n where n.nodeId = :nodeId order by n.revision desc")
                            .setParameter("nodeId", node.getId())
                            .list();
    }

    // Multi-row constraint validation
    public boolean isUniqueWikiname(Node node) {
        Node foundNode = findNodeInArea(node.getParent().getAreaNumber(), node.getWikiname(), entityManager);
        if (foundNode == null) {
            return true;
        } else {
            return node.getId() != null && node.getId().equals(foundNode.getId());
        }
    }

    public Document findDocument(Long documentId) {
        restrictedEntityManager.joinTransaction();

        try {
            return (Document) restrictedEntityManager
                    .createQuery("select d from Document d where d.id = :id")
                    .setParameter("id", documentId)
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    public Directory findDirectory(Long directoryId) {
        restrictedEntityManager.joinTransaction();

        try {
            return (Directory) restrictedEntityManager
                    .createQuery("select d from Directory d where d.id = :id")
                    .setParameter("id", directoryId)
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    public File findFile(Long fileId) {
        restrictedEntityManager.joinTransaction();

        try {
            return (File) restrictedEntityManager
                    .createQuery("select f from File f where f.id = :id")
                    .setParameter("id", fileId)
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    public Document findDefaultDocument(Directory directory) {
        if (directory == null) return null;
        restrictedEntityManager.joinTransaction();
        try {
            return (Document) restrictedEntityManager
                    .createQuery("select doc from Document doc, Directory dir" +
                                 " where doc.id = dir.defaultDocument.id and dir.id = :did")
                    .setParameter("did", directory.getId())
                    .getSingleResult();
        } catch (EntityNotFoundException ex) {
        } catch (NoResultException ex) {
        }
        return null;
    }

    private Session getSession() {
        return ((Session)((org.jboss.seam.persistence.EntityManagerProxy) restrictedEntityManager).getDelegate());
    }
}
