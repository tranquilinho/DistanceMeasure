DistanceMeasure - an ImageJ plugin for measuring distances in fluorescence confocal microscopy.
===============================================================================================

With this plugin, you can measure the distance between two 3D fluorescence distributions.

Features
--------

- LIF file support (experiment series of multichannel stacks)
- export results to Microsoft Excel
- manual refinement of thresholds
- display each channel histogram

Installation and requirements
-----------------------------

Download the required jar files to your ImageJ plugins dir:

- [Distance_Measure.jar](https://github.com/tranquilinho/DistanceMeasure/raw/master/jar/Distance_Measure.jar)
- [loci_tools.jar](http://downloads.openmicroscopy.org/bio-formats/4.3.2/loci_tools.jar) from [BioFormats site](http://loci.wisc.edu/bio-formats/downloads)
- [poi.jar](http://archive.apache.org/dist/poi/release/bin/poi-bin-3.8-20120326.zip) from [Apache POI](http://poi.apache.org/download.html) (unzip and copy the poi.jar inside)
- [forms-1.3.0.jar](http://www.jgoodies.com/download/libraries/forms/forms-1_3_0.zip) from [JGoodies](http://www.jgoodies.com/downloads/archive/) (unzip and copy the forms-1.3.0.jar inside) 
- [file.jar](https://github.com/tranquilinho/JavaGuiComponents/raw/master/jar/file.jar) and [slider.jar](https://github.com/tranquilinho/JavaGuiComponents/raw/master/jar/slider.jar) from [Java GUI Components](https://github.com/tranquilinho/JavaGuiComponents)

Other requirements:

- ImageJ 1.45 or newer
- Java 1.7 or newer
  

Tutorial
--------

The workflow is pretty straighforward: proceed from top to bottom of the window. That is,

1) Import LIF. Select your .LIF file and use this import options:
   - View stack with Hyperstack
   - Color mode: default
   - Autoscale
   
2) Adjust the threshold for each channel (red/green). In the image viewer, you can change channel (C) and slice (Z).


3) Select a region around the cell and "fast add" it to the regions list. If fast add fails, you can use Add. If it takes too long,
you can stop the process and adjust the threshold or the region. You can optionally merge channels.

4) You can export region data as Excel with the "Save Regions" button

About LIF files: they typically have 4 channels and a collection of stacks,
so we may think of it as a 5D image (S Series/experiments x 4 channels x N slices x 2D images)

Contact: jesus.cuenca@gmail.com

License
-------
                                                                                                            
  Authors:
	Jesus Cuenca-Alba (jesus.cuenca@gmail.com)
	CO Sorzano (coss@cnb.csic.es)               
                                                                                                            
  Unidad de  Bioinformatica of Centro Nacional de Biotecnologia , CSIC                                      
                                                                                                            
  This program is free software; you can redistribute it and/or modify                                      
  it under the terms of the GNU General Public License as published by                                      
  the Free Software Foundation; either version 2 of the License, or                                         
  (at your option) any later version.                                                                       
  
  This program is distributed in the hope that it will be useful,                                           
  but WITHOUT ANY WARRANTY; without even the implied warranty of                                            
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                             
  GNU General Public License for more details.                                                              
                                                                                                            
  You should have received a copy of the GNU General Public License                                         
  along with this program; if not, write to the Free Software                                               
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA                                                  
  02111-1307  USA                                                                                           
                                                                                                            
   All comments concerning this program package may be sent to the                                          
   e-mail address 'jesus.cuenca@gmail.com'                                                                  
                                                                                                                                                                                                                                                                

