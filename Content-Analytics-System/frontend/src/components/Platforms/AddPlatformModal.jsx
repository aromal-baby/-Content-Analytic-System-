import React, { useState, useEffect } from 'react';
import platformService from '../../services/PlatformService';

const AddPlatformModal = ({ isOpen, onClose }) => {
    const [contentLink, setContentLink] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [connectedPlatforms, setConnectedPlatforms] = useState([]);

    // Fetch already connected platforms when modal opens
    useEffect(() => {
        if (isOpen) {
            fetchConnectedPlatforms();
        }
    }, [isOpen]);

    const fetchConnectedPlatforms = async () => {
        try {
            const platforms = await platformService.getUserPlatforms();
            setConnectedPlatforms(platforms);
        } catch (error) {
            console.error("Error fetching platforms:", error);
        }
    };

    // Function to detect platform and content info from URL
    const detectPlatformInfo = (url) => {
        // Platform detection patterns
        if (url.includes('youtube.com') || url.includes('youtu.be')) {
            let videoId = '';

            if (url.includes('watch?v=')) {
                videoId = url.split('watch?v=')[1].split('&')[0];
            } else if (url.includes('youtu.be/')) {
                videoId = url.split('youtu.be/')[1].split('?')[0];
            } else if (url.includes('shorts/')) {
                videoId = url.split('shorts/')[1].split('?')[0];
            }

            return {
                platformName: 'YouTube',
                contentId: videoId,
                contentType: url.includes('shorts/') ? 'SHORT' : 'VIDEO'
            };
        }
        else if (url.includes('instagram.com')) {
            let contentId = '';
            let contentType = 'POST';

            if (url.includes('/p/')) {
                contentId = url.split('/p/')[1].split('/')[0];
            } else if (url.includes('/reel/')) {
                contentId = url.split('/reel/')[1].split('/')[0];
                contentType = 'REEL';
            } else if (url.includes('/stories/')) {
                contentId = url.split('/stories/')[1].split('/')[1];
                contentType = 'STORY';
            }

            return {
                platformName: 'Instagram',
                contentId: contentId,
                contentType: contentType
            };
        }
        else if (url.includes('tiktok.com')) {
            let contentId = '';

            if (url.includes('/video/')) {
                contentId = url.split('/video/')[1].split('?')[0];
            }

            return {
                platformName: 'TikTok',
                contentId: contentId,
                contentType: 'VIDEO'
            };
        }
        else if (url.includes('twitter.com') || url.includes('x.com')) {
            let contentId = '';

            if (url.includes('/status/')) {
                contentId = url.split('/status/')[1].split('?')[0];
            }

            return {
                platformName: 'Twitter',
                contentId: contentId,
                contentType: 'TWEET'
            };
        }

        return {
            platformName: 'Other',
            contentId: '',
            contentType: 'UNKNOWN'
        };
    };

    // Function to extract title from URL (new function)
    const extractTitleFromUrl = (url, platformName) => {
        // Basic title extraction logic
        if (platformName === 'YouTube') {
            return `YouTube Video - ${new Date().toLocaleDateString()}`;
        } else if (platformName === 'Instagram') {
            return `Instagram ${url.includes('/reel/') ? 'Reel' : 'Post'} - ${new Date().toLocaleDateString()}`;
        } else if (platformName === 'TikTok') {
            return `TikTok Video - ${new Date().toLocaleDateString()}`;
        } else if (platformName === 'Twitter') {
            return `Tweet - ${new Date().toLocaleDateString()}`;
        }

        return `${platformName} Content - ${new Date().toLocaleDateString()}`;
    };

    // Handle adding content and platform if needed
    const handleAddContent = async () => {
        if (!contentLink.trim()) {
            setError('Please enter a valid content link');
            return;
        }

        setLoading(true);
        try {
            // Detect platform and content info
            console.log("Processing link:", contentLink);
            const contentInfo = detectPlatformInfo(contentLink);

            console.log("Detected:", contentInfo);
            if (contentInfo.contentId === '') {
                setError('Could not detect content ID from the provided link');
                setLoading(false);
                return;
            }

            // Check if platform is already connected
            const platformExists = Array.isArray(connectedPlatforms)
                ? connectedPlatforms.some(platform => platform.platformName === contentInfo.platformName)
                : false;

            let platformId;

            // Generate a default title based on the platform and URL
            const defaultTitle = extractTitleFromUrl(contentLink, contentInfo.platformName);

            console.log("Sending to API:", {
                platformId,
                contentId: contentInfo.contentId,
                contentType: contentInfo.contentType,
                title: defaultTitle // Include a title
            });

            // If platform doesn't exist, create it first
            if (!platformExists) {
                try {
                    const createResponse = await platformService.addPlatform({
                        platformName: contentInfo.platformName,
                        url: contentLink
                    });

                    console.log("Full platform creation response:", createResponse);

                    // Log the raw data for debugging
                    console.log("Raw response data:", createResponse.data);

                    if (createResponse.success) {
                        // Now the data is guaranteed to be an object, not a string
                        if (createResponse.data && createResponse.data.id) {
                            platformId = createResponse.data.id;
                            console.log("Successfully extracted platform ID:", platformId);
                        } else {
                            console.error("Response data structure:", JSON.stringify(createResponse.data));
                            throw new Error("Could not find platform ID in response data");
                        }
                    } else {
                        console.error("Platform creation failed with unsuccessful response", createResponse);
                        throw new Error("Platform creation response indicated failure");
                    }
                } catch (error) {
                    console.error("Platform creation failed:", error);
                    setError(`Platform creation failed: ${error.message}`);
                    setLoading(false);
                    return;
                }
            } else {
                // Find existing platform ID
                const platform = connectedPlatforms.find(
                    p => p.platformName === contentInfo.platformName
                );

                if (!platform || !platform.id) {
                    console.error("Could not find existing platform ID");
                    setError("Error: Could not find platform ID");
                    setLoading(false);
                    return;
                }

                platformId = platform.id;
                console.log("Using existing platform ID:", platformId);
            }

            // Now check that platformId is valid before proceeding
            if (!platformId) {
                setError("Error: Missing platform ID");
                setLoading(false);
                return;
            }

            // Now add the content to the respective platform
            console.log("About to add content with data:", {
                platformId,
                platformContentId: contentInfo.contentId,
                contentType: contentInfo.contentType,
                url: contentLink,
                title: defaultTitle
            });

            const contentAddResponse = await platformService.addContent({
                platformId: platformId,
                platformContentId: contentInfo.contentId,
                contentType: contentInfo.contentType,
                url: contentLink,
                title: defaultTitle  // Send the generated title
            });

            console.log("Content added successfully:", contentAddResponse);

            // Reset and close modal
            setContentLink('');
            setError('');
            onClose();

            console.log("Redirecting to dashboard to refresh content");
            window.location.href = '/dashboard';
        } catch (err) {
            setError('Failed to process content. Please try again.');
            console.error("Content addition error:", err);
            if (err.response) {
                console.error("Error response data:", err.response.data);
                console.error("Error status:", err.response.status);
            }
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center" style={{ zIndex: 9999 }}>
            <div className="bg-white p-6 rounded-lg shadow-xl w-96" style={{ zIndex: 10000 }}>
                <h2 className="text-xl font-bold mb-4">Add Content</h2>

                <input
                    type="text"
                    value={contentLink}
                    onChange={(e) => setContentLink(e.target.value)}
                    placeholder="Paste video/reel/post link here"
                    className="w-full p-2 border rounded mb-4"
                    style={{ color: 'black' }}
                />

                {error && (
                    <p className="text-red-500 mb-4">{error}</p>
                )}

                <div className="flex justify-end space-x-2">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 bg-gray-200 rounded"
                        disabled={loading}
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleAddContent}
                        className="px-4 py-2 bg-blue-500 text-white rounded"
                        disabled={loading}
                    >
                        {loading ? 'Processing...' : 'Add Content'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AddPlatformModal;