#include <bits/stdc++.h>
#include <vector>
#include <jni.h>
#include "com_example_UC_Backend_Extra_RangeChecker.h"
#include "Graph.h"
#include "Map.h"
#include "ShortestPathAlgo.h"

using namespace std;

typedef pair<int, int> Edge;

string jstringToString(JNIEnv *env, jstring jStr) {
    const char *chars = env->GetStringUTFChars(jStr, NULL);
    if (chars == NULL) {
        return "";
    }
    string str(chars);
    env->ReleaseStringUTFChars(jStr, chars);
    return str;
}



JNIEXPORT jint JNICALL Java_com_example_UC_1Backend_Extra_RangeChecker_getAgentsInRange
(JNIEnv *env, jobject obj, jstring str1, jstring str2) {
    
    if (!str1 || !str2) {
        return -1;
    }
    
    const char *cStr1 = env->GetStringUTFChars(str1, nullptr);
    const char *cStr2 = env->GetStringUTFChars(str2, nullptr);
    
    if (!cStr1 || !cStr2) {
        return -2;
    }
    
    string loc1(cStr1);
    string loc2(cStr2);
    
    env->ReleaseStringUTFChars(str1, cStr1);
    env->ReleaseStringUTFChars(str2, cStr2);
    
    map<string, int> fake_map;
    vector<vector<Edge> > graph;
    initializeMap(fake_map);
    initializeGraph(graph);
    
    auto it1 = fake_map.find(loc1);
    auto it2 = fake_map.find(loc2);
    
    if (it1 != fake_map.end() && it2 != fake_map.end()) {
        return static_cast<jint>(findShortestPath(it1->second, it2->second, graph, 27));
    }
    
    return -3;
}
