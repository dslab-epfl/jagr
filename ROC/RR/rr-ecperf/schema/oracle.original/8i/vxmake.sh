
# These commands will automatically allocate disks 
#vxassist -g benchdg make volname length layout=stripe nstripe=1 group=dba user=oracle

vxassist -g benchdg make sysg 100m user=dbbench group=dba
vxassist -g benchdg make tempg 100m user=dbbench group=dba
vxassist -g benchdg make logg1 100m user=dbbench group=dba
vxassist -g benchdg make logg2 100m user=dbbench group=dba
vxassist -g benchdg make rollg 10m user=dbbench group=dba

vxassist -g benchdg make custg1 10M user=dbbench group=dba
vxassist -g benchdg make suppg1 10M user=dbbench group=dba
vxassist -g benchdg make siteg1 10M user=dbbench group=dba
vxassist -g benchdg make partsg1 10M user=dbbench group=dba

vxassist -g benchdg make syso 100m user=dbbench group=dba
vxassist -g benchdg make tempo 100m user=dbbench group=dba
vxassist -g benchdg make logo1 100m user=dbbench group=dba
vxassist -g benchdg make logo2 100m user=dbbench group=dba
vxassist -g benchdg make rollo 10m user=dbbench group=dba

vxassist -g benchdg make custo1 10M user=dbbench group=dba
vxassist -g benchdg make ordo1 10M user=dbbench group=dba
vxassist -g benchdg make ordlo1 10M user=dbbench group=dba
vxassist -g benchdg make itemo1 10M user=dbbench group=dba

vxassist -g benchdg make sysm 100m user=dbbench group=dba
vxassist -g benchdg make tempm 100m user=dbbench group=dba
vxassist -g benchdg make logm1 100m user=dbbench group=dba
vxassist -g benchdg make logm2 100m user=dbbench group=dba
vxassist -g benchdg make rollm 10m user=dbbench group=dba

vxassist -g benchdg make bomm1 10M user=dbbench group=dba
vxassist -g benchdg make invm1 10M user=dbbench group=dba
vxassist -g benchdg make wom1 10M user=dbbench group=dba
vxassist -g benchdg make pom1 10M user=dbbench group=dba

vxassist -g benchdg make syss 100m user=dbbench group=dba
vxassist -g benchdg make temps 100m user=dbbench group=dba
vxassist -g benchdg make logs1 100m user=dbbench group=dba
vxassist -g benchdg make logs2 100m user=dbbench group=dba
vxassist -g benchdg make rolls 10m user=dbbench group=dba

vxassist -g benchdg make custs1 10M user=dbbench group=dba
vxassist -g benchdg make partss1 10M user=dbbench group=dba
vxassist -g benchdg make ordss1 10M user=dbbench group=dba

