# Release Process

This guide provides a chronological steps which goes through release tagging, staging, verification and publishing.

To see the original Jboss Seam guide, see [release-process.txt](release-process.txt)

## Check the SNAPSHOT builds and pass the tests

```bash
mvn clean package install verify -Pdistribution
mvn -Ddependency-check.skip=true clean package install -Pdistribution
```

## Set version and build 

```bash
# change release in poms and distribution/src/assembly/changelog.txt
git -add -A
git commit -S -m 'Release <2.3.12>'
mvn -Ddependency-check.skip=true clean package install  -Pdistribution
git tag -a <2.3.12> -m "Tagging release <2.3.12>"
git push --tags
```


## Prepare next iteration

```bash
# change release in poms and distribution/src/assembly/changelog.txt
git add -A
git commit -m 'Next release cycle'
```

## Create release and upload artifacts to github
