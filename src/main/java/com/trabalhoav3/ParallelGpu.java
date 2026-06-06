package com.trabalhoav3;

import org.jocl.*;
import static org.jocl.CL.*;
import java.nio.ByteBuffer;
import java.util.List;

public class ParallelGpu {

    private static final String programSource =
        "__kernel void searchKernel(" +
        "   __global const char* text, " +
        "   const int textSize, " +
        "   __global const char* targetWord, " +
        "   const int wordSize, " +
        "   const int exactSearch, " +
        "   __global int* result) " +
        "{ \n" +
        "   int i = get_global_id(0); \n" +
        "   if (i > textSize - wordSize) return; \n" +
        
        "   int match = 1; \n" +
        "   for (int j = 0; j < wordSize; j++) { \n" +
        "       char c = text[i + j]; \n" +
        "       if (c >= 'A' && c <= 'Z') c += 32; \n" +
        "       char p = targetWord[j]; \n" +
        "       if (c != p) { \n" +
        "           match = 0; \n" +
        "           break; \n" +
        "       } \n" +
        "   } \n" +
        
        "   if (match == 1 && exactSearch == 1) { \n" +
        "       int priorNotAlphanumeric = 1; \n" +
        "       if (i > 0) { \n" +
        "           char prevChar = text[i - 1]; \n" +
        "           if (prevChar < 0 || (prevChar >= 'a' && prevChar <= 'z') || (prevChar >= 'A' && prevChar <= 'Z') || (prevChar >= '0' && prevChar <= '9')) { \n" +
        "               priorNotAlphanumeric = 0; \n" +
        "           } \n" +
        "       } \n" +
        
        "       int postNotAlphanumeric = 1; \n" +
        "       if (i + wordSize < textSize) { \n" +
        "           char nextChar = text[i + wordSize]; \n" +
        "           if (nextChar < 0 || (nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z') || (nextChar >= '0' && nextChar <= '9')) { \n" +
        "               postNotAlphanumeric = 0; \n" +
        "           } \n" +
        "       } \n" +
        
        "       if (priorNotAlphanumeric == 0 || postNotAlphanumeric == 0) match = 0; \n" +
        "   } \n" +
        
        "   if (match == 1) { \n" +
        "       atomic_add(result, 1); \n" +
        "   } \n" +
        "}";

    public static int search(List<ByteBuffer> buffers, byte[] targetWord, boolean exactSearch) {
        int totalOccurences = 0;

        CL.setExceptionsEnabled(true);

        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[0];

        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        int[] numDevicesArray = new int[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        cl_device_id[] devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, numDevices, devices, null);
        cl_device_id device = devices[0];

        cl_context context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);
        @SuppressWarnings("deprecation")
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        cl_program program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kernel = clCreateKernel(program, "searchKernel", null);

        for (ByteBuffer buffer : buffers) {
            
            int textSize = buffer.capacity();
            int[] GpuResult = new int[]{0};

            Pointer textPointer = Pointer.to(buffer);
            Pointer wordPointer = Pointer.to(targetWord);
            Pointer resultPointer = Pointer.to(GpuResult);
            
            Pointer textSizePointer = Pointer.to(new int[]{textSize});
            Pointer wordSizePointer = Pointer.to(new int[]{targetWord.length});
            Pointer exactSearchPointer = Pointer.to(new int[]{exactSearch ? 1 : 0});

            cl_mem memText = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, 
                                             Sizeof.cl_char * textSize, textPointer, null);
                                             
            cl_mem memWord = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, 
                                               Sizeof.cl_char * targetWord.length, wordPointer, null);
                                               
            cl_mem memResult = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, 
                                                 Sizeof.cl_int, resultPointer, null);

            clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memText));
            clSetKernelArg(kernel, 1, Sizeof.cl_int, textSizePointer);
            clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memWord));
            clSetKernelArg(kernel, 3, Sizeof.cl_int, wordSizePointer);
            clSetKernelArg(kernel, 4, Sizeof.cl_int, exactSearchPointer);
            clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(memResult));

            long[] global_work_size = new long[]{textSize};
            
            clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, 
                                   global_work_size, null, 0, null, null);

            clEnqueueReadBuffer(commandQueue, memResult, CL_TRUE, 0, 
                                Sizeof.cl_int, resultPointer, 0, null, null);

            totalOccurences += GpuResult[0];

            clReleaseMemObject(memText);
            clReleaseMemObject(memWord);
            clReleaseMemObject(memResult);
            buffer.rewind();
        }

        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        return totalOccurences;
    }
}