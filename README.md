# EMC Driver for NeoEMF Models
The plugins in this repository extend the [Eclipse Epsilon](http://www.eclipse.org/epsilon/) model management framework to allow it to read and write EMF models stored in a [NeoEMF](http://www.neoemf.com/) instance.

To use the driver, install Epsilon first (available on Eclipse Marketplace), then install all the features from this update site and restart Eclipse:
```
https://gdaniel.github.io/emc-neoemf
```

You should be able to use a new "NeoEMF Model" model type within E*L launch configuration. You may need to check the "Show all model types" checkbox in order to see it, though. The model type will ask for the URI of the NeoEMF resource, and the type of backend used to store the model (current version supports Graph and Map implementations).

You can configure NeoEMF with a set of caching, autocommit, and database-specific options in order to optimize execution time and memory consumption.

This driver takes advantage of NeoEMF's efficient implementation of `X.allInstances()` and uses the global EPackage registry. Additional optimizations will be added in the future, such as efficient attribute/association navigation.

## Acknowledgement
This set of plugins has been inspired by the [EMC Driver for CDO Models](https://github.com/epsilonlabs/emc-cdo) developed by [bluezio](https://github.com/bluezio).
