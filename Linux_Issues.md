# Known Linux Issues #

### Regional Settings ###

  1. When regional settings are set to something other than English using the GTK+ Look and Feel will cause Buffered Images to work improperly. It seems that under JDK6 the GTK+ LAF is still buggy and does not have full regional setting support. This has been verified by testing under OpenJDK6 and Oracles' JRE/JDK6. The solution is to use the default cross platform LAF (metal) when the regional settings are other than English.

